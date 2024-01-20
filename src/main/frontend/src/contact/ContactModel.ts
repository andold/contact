// ContactModel.ts
export default interface ContactModel {
	id?: number;
	content: string;
	vcard: any[];
	created?: string;
	updated?: string;
	maps: ContactMapModel[];
}
export interface ContactMapModel {
	id?: number;
	vcardId: number;
	key: string;
	value: number;
	created?: string;
	updated?: string;
}
