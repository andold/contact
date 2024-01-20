// src/main/frontend/src/setProxy.js

import { createProxyMiddleware } from "http-proxy-middleware";

export default function(app) {
	app.use(createProxyMiddleware("/api", {
		target: "http://localhost:8080/contact",
		changeOrigin: true,
	}));
};
