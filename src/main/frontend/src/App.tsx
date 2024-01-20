import "./App.css";
import "bootstrap/dist/css/bootstrap.min.css";
import "ag-grid-community/dist/styles/ag-grid.css";
import "ag-grid-community/dist/styles/ag-theme-balham.css";
import "ag-grid-community/dist/styles/ag-theme-balham-dark.css";

import React, { useEffect, useMemo, useRef, useState } from "react";
import { DndProvider } from "react-dnd";
import { HTML5Backend } from "react-dnd-html5-backend";
import moment from "moment-timezone";
import "moment/locale/ko";

// container
import ContactContainter from "./contact/ContactContainter";

// App.tsx
export default function App() {
	const ref = useRef(null);

	useEffect(() => {
		moment.tz.setDefault("Asia/Seoul");
		moment.locale("ko");
	}, []);
	return (
		<div ref={ref} className="App">
			<DndProvider backend={HTML5Backend}>
				<ContactContainter width={1024} fontsize={12} />
			</DndProvider>
		</div>
	);
}
