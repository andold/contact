package kr.andold.contact;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import ezvcard.Ezvcard;
import ezvcard.VCard;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ContactService {
	@Autowired
	private ContactRepository repository;
	@Autowired
	private ContactMapRepository mapRepository;

	public List<ContactEntity> search() {
		return repository.findAll();
	}

	public List<ContactDomain> search(ContactParam param) {
		List<ContactEntity> entities;
		if (param == null || param.getKeyword() == null || param.getKeyword().isBlank()) {
			entities = repository.findAll();
		} else {
			entities = repository.findByContentContains(param.getKeyword());
		}

		List<ContactDomain> domains = new ArrayList<ContactDomain>();
		for (ContactEntity entity : entities) {
			ContactDomain domain = ContactDomain.of(entity);
			domains.add(domain);
		}
		return domains;
	}

	public int batch(ContactParam param) {
		if (param == null) {
			return 0;
		}

		int count = 0;
		List<ContactDomain> creates = param.getCreates();
		List<ContactDomain> updates = param.getUpdates();
		List<ContactDomain> removes = param.getRemoves();

		if (creates != null) {
			List<ContactDomain> created = create(creates);
			count += Utility.size(created);
		}
		if (removes != null) {
			count += delete(removes);
		}
		if (updates != null) {
			count += Utility.size(update(updates));
		}

		return count;
	}

	private List<ContactDomain> update(List<ContactDomain> updates) {
		List<ContactEntity> entities = new ArrayList<ContactEntity>();
		Date date = new Date();
		for (ContactDomain contact : updates) {
			ContactEntity entity = ContactDomain.toEntity(contact);
			entities.add(entity);
			entity.setUpdated(date);
		}

		List<ContactEntity> list = repository.saveAllAndFlush(entities);
		List<ContactDomain> domains = new ArrayList<ContactDomain>();
		for (ContactEntity entity : list) {
			ContactDomain domain = ContactDomain.of(entity);
			domains.add(domain);
		}
		return domains;
	}

	public ContactDomain update(Integer id, ContactDomain afterDomain) {
		Optional<ContactEntity> beforeEntity = read(id);
		if (!beforeEntity.isPresent()) {
			return null;
		}

		ContactDomain beforeDomain = ContactDomain.of(beforeEntity.get());
		ContactDomain mergedDomain = merge(beforeDomain, afterDomain);
		ContactEntity mergedEntity = ContactDomain.toEntity(mergedDomain);
		mergedEntity.setUpdated(new Date());
		ContactEntity updated = repository.saveAndFlush(mergedEntity);
		List<ContactMapEntity> maps = mergedEntity.getMaps();
		if (maps != null) {
			for (ContactMapEntity map : maps) {
				map.setUpdated(new Date());
				if (map.getVcardId() == null)
					map.setVcardId(id);
				if (map.getCreated() == null)
					map.setCreated(new Date());
			}

			mapRepository.saveAllAndFlush(maps);
		}
		return ContactDomain.of(updated);
	}

	private Optional<ContactEntity> read(Integer id) {
		return repository.findById(id);
	}

	@Transactional
	@Modifying
	private int delete(List<ContactDomain> removes) {
		List<ContactEntity> entities = new ArrayList<ContactEntity>();
		for (ContactDomain contact : removes) {
			ContactEntity entity = ContactDomain.toEntity(contact);
			entities.add(entity);
		}

		try {
			repository.deleteAll(entities);
			repository.flush();
			return Utility.size(entities);
		} catch (Exception e) {
			log.warn("{} delete(#{}) - Exception:: {}", Utility.indentMiddle(), Utility.size(removes), e.getLocalizedMessage());
			int deleted = 0;
			for (ContactEntity entity : entities) {
				try {
					repository.delete(entity);
					deleted++;
				} catch (Exception f) {
					log.warn("{} delete(#{}) - {} - Exception:: {}", Utility.indentMiddle(), Utility.size(removes), entity, f.getLocalizedMessage());
				}
			}
			repository.flush();
			return deleted;
		}
	}

	@Transactional
	@Modifying
	private List<ContactDomain> create(List<ContactDomain> creates) {
		List<ContactEntity> entities = new ArrayList<ContactEntity>();
		Date date = new Date();
		for (ContactDomain contact : creates) {
			ContactEntity entity = ContactDomain.toEntity(contact);
			entities.add(entity);
			entity.setId(null);
			entity.setCreated(date);
			entity.setUpdated(date);
		}

		try {
			List<ContactEntity> list = repository.saveAllAndFlush(entities);
			List<ContactDomain> domains = new ArrayList<ContactDomain>();
			for (ContactEntity entity : list) {
				ContactDomain domain = ContactDomain.of(entity);
				domains.add(domain);
			}

			return domains;
		} catch (Exception e) {
			log.warn("{} create(#{}) - Exception:: {}", Utility.indentMiddle(), Utility.size(creates), e.getLocalizedMessage());

			List<ContactDomain> domains = new ArrayList<ContactDomain>();
			for (ContactEntity entity : entities) {
				try {
					ContactEntity created = repository.save(entity);
					ContactDomain domain = ContactDomain.of(created);
					domains.add(domain);
				} catch (Exception f) {
					log.warn("{} create(#{}) - {} - Exception:: {}", Utility.indentMiddle(), Utility.size(creates), entity, f.getLocalizedMessage());
				}
			}
			repository.flush();

			return domains;
		}
	}

	public ContactParam upload(MultipartFile file) throws UnsupportedEncodingException, IOException {
		log.info("{} upload({})", Utility.indentStart(), Utility.toStringJson(file, 64, 32));
		long started = System.currentTimeMillis();

		String text = Utility.extractStringFromText(file.getInputStream());
		String contentType = file.getContentType();
		List<ContactDomain> domains = new ArrayList<ContactDomain>();
		switch (contentType) {
			case "text/x-vcard":
				List<VCard> vcards = Ezvcard.parse(text).all();
				for (VCard vcard : vcards) {
					ContactDomain domain = ContactDomain.of(vcard);
					if (domain == null) {
						continue;
					}

					domains.add(domain);
				}

				break;
			case "application/json":
				String[] lines = text.split("\n");
				for (String line : lines) {
					ContactDomain domain = ContactDomain.of(line);
					if (domain == null) {
						continue;
					}

					domains.add(domain);
				}

				break;

			default:
				break;
		}

		ContactParam result = differ(domains);

		log.info("{} {} - upload({}) - {}", Utility.indentEnd(), result, Utility.toStringJson(file, 32, 32), Utility.toStringPastTimeReadable(started));
		return result;
	}

	private ContactParam differ(List<ContactDomain> after) {
		List<ContactEntity> beforeContacts = search();
		List<ContactDomain> before = new ArrayList<ContactDomain>();
		for (ContactEntity entity : beforeContacts) {
			before.add(ContactDomain.of(entity));
		}
		ContactParam result = differ(before, after);
		return result;
	}

	private ContactParam differ(List<ContactDomain> beforeDomains, List<ContactDomain> afterDomains) {
		Map<String, ContactDomain> mapBefore = makeMap(beforeDomains);
		Map<String, ContactDomain> mapAfter = makeMap(afterDomains);
		ContactParam result = ContactParam.builder().creates(new ArrayList<ContactDomain>()).duplicates(new ArrayList<ContactDomain>()).updates(
			new ArrayList<ContactDomain>()).removes(new ArrayList<ContactDomain>()).build();
		for (String key : mapBefore.keySet()) {
			ContactDomain before = mapBefore.get(key);
			ContactDomain after = mapAfter.get(key);
			if (after == null) {
				result.getRemoves().add(before);
				continue;
			}

			if (isSame(before, after)) {
				result.getDuplicates().add(before);
				continue;
			}

			if (isSuperset(before, after)) {
				result.getDuplicates().add(before);
				continue;
			}

			ContactDomain merged = merge(before, after);
			result.getUpdates().add(merged);
		}
		for (String key : mapAfter.keySet()) {
			ContactDomain after = mapAfter.get(key);
			ContactDomain before = mapBefore.get(key);
			if (before == null) {
				result.getCreates().add(after);
				continue;
			}
		}

		return result;
	}

	// field key is const key = `${clone.id}.${x[0]}:${JSON.stringify(x)}`;
	private ContactDomain merge(ContactDomain beforeDomain, ContactDomain afterDomain) {
		ContactDomain mergedDomain = ContactDomain.of(beforeDomain);
		@SuppressWarnings("unchecked") List<List<Object>> beforeFields = (List<List<Object>>)beforeDomain.getVcard().get(1);
		@SuppressWarnings("unchecked") List<List<Object>> afterFields = (List<List<Object>>)afterDomain.getVcard().get(1);
		List<Object> fields = mergeField(beforeFields, afterFields);

		mergedDomain.getVcard().set(1, fields);
		String json = Utility.toStringJson(mergedDomain.getVcard());
		VCard vcard = Ezvcard.parseJson(json).first();
		String content = Ezvcard.write(vcard).go();
		mergedDomain.setContent(content);

		Date date = new Date();
		Map<String, ContactMapEntity> beforeMap = makeMapMap(beforeDomain.getMaps());
		Map<String, ContactMapEntity> afterMap = makeMapMap(afterDomain.getMaps());
		for (String key : afterMap.keySet()) {
			ContactMapEntity afterMapEntity = afterMap.get(key);
			ContactMapEntity beforeMapEntity = beforeMap.get(key);
			if (beforeMapEntity == null) {
				if (afterMapEntity.getVcardId() == null) {
					afterMapEntity.setVcardId(beforeDomain.getId());
				}
				if (afterMapEntity.getCreated() == null) {
					afterMapEntity.setCreated(date);
				}
				if (afterMapEntity.getUpdated() == null) {
					afterMapEntity.setUpdated(date);
				}
				beforeMap.put(key,  afterMapEntity);
				continue;
			}
			
			beforeMapEntity.setValue(afterMapEntity.getValue());
		}
		
		List<ContactMapEntity> maps = new ArrayList<ContactMapEntity>(beforeMap.values());
		mergedDomain.setMaps(maps);
		return mergedDomain;
	}

	private List<Object> mergeField(List<List<Object>> beforeFields, List<List<Object>> afterFields) {
		Map<String, Object> map = new HashMap<>();
		for (List<Object> field : afterFields) {
			String key  = keyField(null, field);
			map.put(key, field);
		}
		for (List<Object> field : beforeFields) {
			String key  = keyField(null, field);
			map.put(key, field);
		}

		List<Object> fields = new ArrayList<Object>(map.values());
		return fields;
	}

	private String keyField(ContactDomain domain, List<Object> field) {
		if (domain == null || domain.getId() == null) {
			String key =  String.format("%d.%s:%s", 0, field.get(0), Utility.toStringJson(field));
			return key;
		}

		String key = String.format("%d.%s:%s", domain.getId(), field.get(0), Utility.toStringJson(field));
		return key;
	}

	private Map<String, List<Object>> makeMapByField(List<List<Object>> fields) {
		Map<String, List<Object>> map = new HashMap<String, List<Object>>();
		if (fields == null) {
			return map;
		}
		
		for (List<Object> field : fields) {
			String key = keyField(null, field);
			map.put(key, field);
		}

		return map;
	}

	private boolean isSuperset(ContactDomain beforeDomain, ContactDomain afterDomain) {
		@SuppressWarnings("unchecked") List<List<Object>> beforeFields = (List<List<Object>>)beforeDomain.getVcard().get(1);
		@SuppressWarnings("unchecked") List<List<Object>> afterFields = (List<List<Object>>)afterDomain.getVcard().get(1);
		Map<String, List<Object>> mapBefore = makeMapByField(beforeFields);
		for (List<Object> field : afterFields) {
			String key = keyField(null, field);
			Object before = mapBefore.get(key);
			if (before == null) {
				return false;
			}
		}

		return true;
	}

	private boolean isSame(ContactDomain before, ContactDomain after) {
		return before.toString().compareToIgnoreCase(after.toString()) == 0;
	}

	private Map<String, ContactDomain> makeMap(List<ContactDomain> list) {
		Map<String, ContactDomain> map = new HashMap<String, ContactDomain>();
		if (list == null) {
			return map;
		}

		for (ContactDomain domain : list) {
			String key = key(domain);
			map.put(key, domain);
		}

		return map;
	}

	@SuppressWarnings("unchecked")
	private String key(ContactDomain domain) {
		List<Object> listObject = (List<Object>)domain.getVcard().get(1);

		if (Utility.size(listObject) > 0) {
			String key = String.format("%s.%s", fieldValue(listObject, "fn", "N/A"), fieldValue(listObject, "n", "N/A"));
			return key;
		}

		for (Object object : listObject) {
			if (!(object instanceof ArrayList)) {
				continue;
			}

			List<Object> list = (List<Object>)object;
			if (list.size() < 4) {
				continue;
			}

			Object first = list.get(0);
			if (!(first instanceof String)) {
				continue;
			}

			String name = (String)first;
			if (name.compareToIgnoreCase("fn") != 0) {
				continue;
			}

			Object fourth = list.get(3);
			if (!(fourth instanceof String)) {
				continue;
			}

			String fname = (String)fourth;
			String key = String.format("%s", fname);
			return key;
		}

		return Double.toString(Math.random());
	}

	@SuppressWarnings("unchecked")
	private String fieldValue(List<Object> listObject, String field, String defaultValue) {
		for (Object object : listObject) {
			if (!(object instanceof ArrayList)) {
				continue;
			}

			List<Object> list = (List<Object>)object;
			if (list.size() < 4) {
				continue;
			}

			Object first = list.get(0);
			if (!(first instanceof String)) {
				continue;
			}

			String name = (String)first;
			if (name.compareToIgnoreCase(field) != 0) {
				continue;
			}

			Object fourth = list.get(3);
			if (!(fourth instanceof String)) {
				continue;
			}

			String fname = (String)fourth;
			return fname;
		}

		return defaultValue;
	}

	public String download() {
		StringBuffer stringBuffer = new StringBuffer("");
		List<ContactEntity> contacts = search();
		if (contacts == null) {
			return stringBuffer.toString();
		}

		for (ContactEntity contact : contacts) {
			stringBuffer.append(contact.toString());
			stringBuffer.append("\n");
		}

		return stringBuffer.toString();
	}

	public String downloadVCard(Integer priority) {
		if (priority == null) {
			priority = Integer.MAX_VALUE;
		}

		StringBuffer stringBuffer = new StringBuffer("");
		List<ContactDomain> domains = search(null);
		if (domains == null) {
			// 데이터 없음
			return stringBuffer.toString();
		}

		for (ContactDomain contact : domains) {
			stringBuffer.append(downloadVCard(priority, contact));
		}

		return stringBuffer.toString();
	}

	private String downloadVCard(int priority, ContactDomain domain) {
		List<ContactMapEntity> maps = domain.getMaps();
		Map<String, ContactMapEntity> map = makeMapMap(maps);
		if (priority(map, domain.getId().toString(), 0) > priority) {
			return "";
		}
		
		List<Object> vcardFromJson = domain.getVcard();
		@SuppressWarnings("unchecked") List<List<Object>> fields = (List<List<Object>>)vcardFromJson.get(1);
		Iterator<List<Object>> iterator = fields.iterator();
		while (iterator.hasNext()) {
			List<Object> field = iterator.next();
			if (priority(map, domain, field, 0) > priority) {
				iterator.remove();
			}
		}
		String json = Utility.toStringJson(vcardFromJson);
		VCard vcard = Ezvcard.parseJson(json).first();
		String result = Ezvcard.write(vcard).go();
		return result;
	}

	private int priority(Map<String, ContactMapEntity> map, ContactDomain domain, List<Object> field, int defaultValue) {
		String key = String.format("%d.%s:%s", domain.getId(), field.get(0), Utility.toStringJson(field));
		return priority(map, key, defaultValue);
	}

	private int priority(Map<String, ContactMapEntity> map, String key, int defaultValue) {
		ContactMapEntity entity = map.get(key);
		if (entity == null) {
			return defaultValue;
		}

		Integer value = entity.getValue();
		if (value == null) {
			return defaultValue;
		}

		return value.intValue();
	}

	private Map<String, ContactMapEntity> makeMapMap(List<ContactMapEntity> maps) {
		Map<String, ContactMapEntity> map = new HashMap<String, ContactMapEntity>();
		if (maps == null) {
			return map;
		}

		for (ContactMapEntity entity : maps) {
			map.put(entity.getKey(), entity);
		}

		return map;
	}

}
