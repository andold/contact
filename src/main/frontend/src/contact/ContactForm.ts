import ContactModel from "./ContactModel";

// ContactForm.ts
export default interface ContactForm {
	mode: number;
	keyword: string;
	cutline: number;
	updating: ContactModel;
	showUploadModal: boolean;
	working: string[];
	
	map: any;
}
