package kr.andold.contact;

import java.net.URLDecoder;
import java.util.List;

import org.springframework.beans.BeanUtils;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import ezvcard.Ezvcard;
import ezvcard.VCard;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Data
public class ContactDomain extends ContactEntity {
	private List<Object> vcard;

	public static ContactEntity toEntity(ContactDomain param) {
		String json = Utility.toStringJson(param.getVcard());
		VCard vcard = Ezvcard.parseJson(json).first();
		return ContactEntity.builder()
			.id(param.getId())
			.content(Ezvcard.write(vcard).go())
			.created(param.getCreated())
			.updated(param.getUpdated())
			.maps(param.getMaps())
			.build();
	}

	public ContactDomain(ContactEntity entity) {
		BeanUtils.copyProperties(entity, this);
	}

	@Override
	public String toString() {
		return Utility.toStringJson(this);
	}

	public static ContactDomain of(ContactEntity entity) {
		ContactDomain domain = new ContactDomain(entity);
		VCard vcard = Ezvcard.parse(entity.getContent()).first();
		String json = Ezvcard.writeJson(vcard).go();
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.setSerializationInclusion(Include.NON_NULL);
		objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		try {
			@SuppressWarnings("unchecked") List<Object> listObject = objectMapper.readValue(json, List.class);
			domain.setVcard(listObject);
			return domain;
		} catch (JsonProcessingException e) {
			log.warn("{} Contact2Domain({}) - JsonProcessingException:: {}", entity, e.getLocalizedMessage());
		}

		return domain;
	}

	public static ContactDomain of(String string) {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.setSerializationInclusion(Include.NON_NULL);
		objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		try {
			ContactEntity entity = objectMapper.readValue(string, ContactEntity.class);
			return ContactDomain.of(entity);
		} catch (Exception e) {
			try {
				return objectMapper.readValue(URLDecoder.decode(string, "UTF-8"), ContactDomain.class);
			} catch (Exception f) {
				e.printStackTrace();
				f.printStackTrace();
			}
		}

		return null;
	}

	public static ContactDomain of(VCard vcard) {
		ContactDomain domain = new ContactDomain();
		String json = Ezvcard.writeJson(vcard).go();
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.setSerializationInclusion(Include.NON_NULL);
		objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		try {
			@SuppressWarnings("unchecked") List<Object> listObject = objectMapper.readValue(json, List.class);
			return ContactDomain.builder()
				.content(Ezvcard.write(vcard).go())
				.vcard(listObject)
				.build();
		} catch (JsonProcessingException e) {
			log.warn("{} Contact2Domain({}) - JsonProcessingException:: {}", vcard, e.getLocalizedMessage());
		}

		return domain;
	}

}
