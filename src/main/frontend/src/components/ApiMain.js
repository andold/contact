import axios from 'axios';

/**
 * ApiMain.js
 * main section
 */
let MAIN_ENVIRONMENT = null;
export function apiMainEnvironment(request, onSuccess, onError, element) {
	if (!MAIN_ENVIRONMENT) {
		//console.debug("apiMainEnvironment", request, onSuccess, onError, element);
		axios.get("./api/environment", request)
			.then(response => {
				MAIN_ENVIRONMENT = response.data;
				onSuccess(request, MAIN_ENVIRONMENT, element);
			})
			.catch(error => onError(request, error, element));
		return;
	}

	onSuccess(request, MAIN_ENVIRONMENT, element);
}
