package kr.andold.contact;

import java.time.Instant;
import java.util.Date;
import java.util.List;

import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Entity
@Table(name = "vcard")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@Data
public class ContactEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Integer id;

	@Column(name = "content")
	private String content;

	@Column(name = "created")
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ", timezone = "Asia/Seoul")
	@Temporal(TemporalType.TIMESTAMP)
	private Date created;

	@Column(name = "updated")
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ", timezone = "Asia/Seoul")
	@Temporal(TemporalType.TIMESTAMP)
	private Date updated;

	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "vcard_id")
	@NotFound(action = NotFoundAction.IGNORE)
	private List<ContactMapEntity> maps;

	@Override
	public String toString() {
		return Utility.toStringJson(this);
	}

	public static ContactEntity sample() {
		return ContactEntity.builder().id(23).content(
			"BEGIN:VCARD\\nVERSION:4.0\\nPRODID:ez-vcard 0.12.0\\nFN:CJ헬로모바일\\nN:;CJ헬로모바일;;;\\nEMAIL;TYPE=WORK:mireaob@naver.com\\nTEL;TYPE=VOICE:1688-0022\\nCATEGORIES:company,myContacts\\nNOTE:\\n 그룹:company\\n\\n 그룹:company\\n 그룹:company\\nEND:VCARD").created(
				Date.from(Instant.parse("2023-03-15 22:39"))).updated(Date.from(Instant.parse("2023-03-15 22:39"))).build();
	}

}
