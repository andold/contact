package kr.andold.contact;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Data
public class ContactParam extends ContactDomain {
	private String keyword;

	private List<ContactDomain> creates;
	private List<ContactDomain> duplicates;
	private List<ContactDomain> updates;
	private List<ContactDomain> removes;

	@Override
	public String toString() {
		return String.format("ContactParam(creates: #%d, duplicates: #%d, updates: #%d, removes: #%d)", Utility.size(creates), Utility.size(duplicates),
			Utility.size(updates), Utility.size(removes));
	}
}
