import { makeAutoObservable } from "mobx";
import moment from "moment";
import repository from './ContactRepository';

// domain
import ContactModel, { ContactMapModel } from "./ContactModel";

const cellStyleLeft = { textAlign: "left", padding: 1, };
const cellStyleRight = { textAlign: "right", padding: 1, paddingRight: 4, };
const cellStyleCenter = { textAlign: "center", padding: 1, };
// ContactStore.ts
class ContactStore {
	constructor() {
		makeAutoObservable(this);
	}

	batch(request: any, onSuccess?: any, onError?: any, element?: any) {
		repository.batch(request, onSuccess, onError, element);
	}
	create(request: any, onSuccess?: any, onError?: any, element?: any) {
		repository.create(request, onSuccess, onError, element);
	}
    search(request: any, onSuccess?: any, onError?: any, element?: any) {
		repository.search(request, onSuccess, onError, element);
    }
	update(request: ContactModel, onSuccess?: any, onError?: any, element?: any) {
		repository.update(request, onSuccess, onError, element);
	}
	remove(request: any, onSuccess?: any, onError?: any, element?: any) {
		repository.remove(request, onSuccess, onError, element);
	}
	upload(request?: any, onSuccess?: any, onError?: any, element?: any) {
		repository.upload(request, onSuccess, onError, element);
	}
	download(request?: any, onSuccess?: any, onError?: any, element?: any) {
		repository.download(request, onSuccess, onError, element);
	}
	downloadVcard(request?: any, onSuccess?: any, onError?: any, element?: any) {
		if (!request) {
			request = {
				filename: `contact-${moment().format("YYYYMMDD")}.vcf`,
				priority: 1024,
			};
		}
		repository.downloadVcard(request, onSuccess, onError, element);
	}


	//	utils
	columnDefs(hides?: string[]): any {
		return [{
			field: "id",
			hide: hides && hides.includes("id"),
			editable: false,
			cellStyle: cellStyleRight,
			width: 64,
			rowDrag: true,
		}, {
			field: "content",
			hide: hides && hides.includes("content"),
			editable: false,
			cellStyle: cellStyleLeft,
			width: 128,
			flex: 1,
		}, {
			field: "created",
			hide: hides && hides.includes("created"),
			editable: false,
			valueFormatter: (params: any) => moment(params.value).format("YYYY-MM-DD"),
			width: 64,
			cellStyle: cellStyleCenter,
		}, {
			field: "updated",
			hide: hides && hides.includes("updated"),
			editable: false,
			valueFormatter: (params: any) => moment(params.value).format("YYYY-MM-DD"),
			width: 64,
			cellStyle: cellStyleCenter,
		}];
	}
	bgByPriority(priority: number): import("csstype").Property.BackgroundColor {
			if (!Number.isInteger(priority)) {
			priority = 0;
		}
		const table: any[] = [
			[4, "secondary"],
			[8, "success"],
			[12, "info"],
			[16, "warning"],
		];
		for (let cx = 0; cx < table.length; cx++) {
			if (priority < table[cx][0]) {
				return table[cx][1];
			}

		}
		return "danger";
	}
	colorByPriority(priority: number): import("csstype").Property.BackgroundColor {
		if (!Number.isInteger(priority)) {
			priority = 0;
		}

		const opacity = 1.0;
		const table: any[] = [
			[3, `rgb(108, 117, 125, ${opacity})`],
			[6, `rgb(25, 135, 84, ${opacity})`],
			[9, `rgb(13, 202, 240, ${opacity})`],
			[12, `rgb(255, 193, 7, ${opacity})`],
		];
		for (let cx = 0; cx < table.length; cx++) {
			if (priority < table[cx][0]) {
				return table[cx][1];
			}

		}
		return `rgb(220, 53, 69, ${opacity})`;
	}
	addKey(map: any, key: string, value: any) {
		//console.log(map.get(key), key, value);
		if (Array.isArray(value)) {
			// array
			value.forEach((item: any) => {
				this.addKey(map, `${key}[]`, item);
			});
			return;
		}
		if (typeof (value) == "object") {
			// object. has child.
			//map.set(key, value);
			Object.getOwnPropertyNames(value).forEach((name: string) => {
				this.addKey(map, `${key}.${name}`, value[name]);
			});
			return;
		}

		// maybe no child. premitive.
		map.set(key, value);
	}
	analyze(contacts: any[]) {
		const map = new Map<string, any>();
		contacts.forEach((contact: any) => {
			Object.getOwnPropertyNames(contact).forEach((name: string) => {
				this.addKey(map, name, contact[name]);
			});
		});
		Array.from(map.keys()).sort().forEach((key: string) => console.log(key, `: 『${map.get(key).toString().replaceAll("\n", "\\n")}』`));
		console.log(map);
	}
	formatter(): any {
		return {
			".Emails[]": function(value: any, map: Map<string, any>, key: string): string {
				return `email: ${JSON.stringify(value)}\n`;
			},
		};
	}
	format(value: any, map?: Map<string, any>, key?: string, formatter?: any): string {
		if (map === undefined && Array.isArray(value)) {
			// start. array of contacts
			map = new Map<string, any>();
			formatter = this.formatter();
			const result = value.reduce((prev: string, item: any) => {
				const string = this.format(item, map, "", formatter);
				return `${prev}${string}` + "\n";
			}, "");
			//Array.from(map.keys()).sort().forEach((key: string) => console.log(key, `: 『${map.get(key).toString().replaceAll("\n", "\\n")}』`));
			//console.log(result);
			return `[${result}]`;
		}

		if (formatter[key]) {
			return formatter[key](value, map, key);
		}
		if (Array.isArray(value)) {
			// array
			const result = value.reduce((prev: string, item: any) => {
				const string = this.format(item, map, `${key}[]`, formatter);
				return `${prev}${string}, `;
			}, "");
			return `[${result}]`;
		}
		if (typeof (value) == "object") {
			// object. has child.
			//map.set(key, value);
			const result = Object.getOwnPropertyNames(value).reduce((prev: string, name: string) => {
				const string = this.format(value[name], map, `${key}.${name}`, formatter);
				return `${prev}${string}, `;
			}, "");
			return `{${result}}`;
		}

		// maybe no child. premitive.
		map.set(key, value);
		return value.toString();
	}
}
export default new ContactStore();

