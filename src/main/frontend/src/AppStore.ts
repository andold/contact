import axios from "axios";

// AppStore.ts
export default new class AppStore {
	count: number = 0;

	constructor() {
	}

	heartbeat(params?: any) {
		this.count++;
		if (params) {
			console.log(this.count, params);
		}
	}
	async test(request?: any, onSuccess?: any, onError?: any, element?: any) {
		return axios.post("./api/household/test", request)
			.then(response => onSuccess && onSuccess(request, response.data, element))
			.catch(error => onError && onError(request, error, element));
	}
	async create(request: any, onSuccess?: any, onError?: any, element?: any) {
		return axios.post("./api/household/create", request)
			.then(response => onSuccess && onSuccess(request, response.data, element))
			.catch(error => onError && onError(request, error, element));
	}
	async update(request: any, onSuccess?: any, onError?: any, element?: any) {
		return axios.post("./api/household/update", request)
			.then(response => onSuccess && onSuccess(request, response.data, element))
			.catch(error => onError && onError(request, error, element));
	}
	async remove(request: any, onSuccess?: any, onError?: any, element?: any) {
		return axios.post("./api/household/remove", request)
			.then(response => onSuccess && onSuccess(request, response.data, element))
			.catch(error => onError && onError(request, error, element));
	}
	async download(request: any, onSuccess?: any, onError?: any, element?: any) {
		return axios({
			url: "./api/household/download",
			method: "GET",
			responseType: "blob",
		}).then(response => {
			const url = window.URL.createObjectURL(new Blob([response.data]));
			const link = document.createElement("a");
			link.href = url;
			link.setAttribute("download", request.filename);
			document.body.appendChild(link);
			link.click();
			link.parentNode.removeChild(link);
			onSuccess && onSuccess(request, response.data, element);
		})
			.catch(error => onError && onError(request, error, element));
	}
	async upload(request: any, onSuccess?: any, onError?: any, element?: any) {
		const formData = new FormData();
		formData.append("file", request.file);
		return axios.post("./api/household/upload", formData)
			.then(response => onSuccess && onSuccess(request, response.data, element))
			.catch(error => onError && onError(request, error, element));
	}
	async compile(request?: any, onSuccess?: any, onError?: any, element?: any) {
		return axios.get("./api/household/compile")
			.then(response => onSuccess && onSuccess(request, response.data, element))
			.catch(error => onError && onError(request, error, element));
	}
}
