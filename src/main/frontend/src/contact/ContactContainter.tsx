import React, { useEffect, useMemo, useRef, useState } from "react";
import { AgGridReact } from "ag-grid-react";
import { Accordion, Button, CloseButton, Col, Form, InputGroup, Modal, Row } from "react-bootstrap";

import "./ContactContainter.css"
// domain

// store
import store from "./ContactStore";
import DragAndDropFile from "./DragAndDropFile";

// view
import ContactView, { Priority, UpdateModal } from "./ContactView";
import ContactModel from "./ContactModel";
import moment from "moment";
import ContactForm from "./ContactForm";
import AppStore from "../AppStore";

// ContactContainter.tsx
export default ((props: any) => {
	AppStore.heartbeat();
	const { } = props;

	const [form, setForm] = useState<ContactForm>({
		mode: 1,
		keyword: "",
		cutline: 15,
		updating: null,
		showUploadModal: false,
		working: [],

		map: new Map(),
	});
	const [updating, setUpdating] = useState<ContactModel>(null);
	const [modes, setModes] = useState([]);

	useEffect(() => {
		setModes([
			<ModeZero
				form={form}
				onChange={(params: any) => setForm({ ...form, ...params, })}
				doUpdate={doUpdate}
			/>,
			<ModeTwo
				form={form}
				onChange={(params: any) => setForm({ ...form, ...params, })}
				doUpdate={doUpdate}
			/>,
		]);
	}, [form]);

	function doUpdate(contact: ContactModel) {
		setUpdating(contact);
	}
	function handleOnUpdate(contact: any) {
		store.update(contact, (request: any, updated: ContactModel) => {
			console.log(request, updated);
			//setForm({ ...form, updating: null, });
		});
	}
	function handleOnClose() {
		//setForm({ ...form, updating: null, });
	}

	return (<>
		<Header
			form={form}
			onChange={(params: any) => setForm({ ...form, ...params, })}
		/>
		{modes[form.mode % modes.length]}
		<Header
			form={form}
			onChange={(params: any) => setForm({ ...form, ...params, })}
		/>
		<Upload
			form={form}
			onChange={(params: any) => setForm({ ...form, ...params, })}
		/>
		<UpdateModal
			contact={updating}
			onClose={handleOnClose}
			onUpdate={handleOnUpdate}
			form={form}
			onChange={(params: any) => setForm({ ...form, ...params, })}
		/>
	</>);
});

function ModeTwo(props: any) {
	const form = props.form as ContactForm;
	const { onChange, doUpdate } = props;

	const [contacts, setContacts] = useState<ContactModel[]>([]);

	useEffect(() => {
		store.search(form, (_: any, result: ContactModel[]) => {
			setContacts(result.sort((a: ContactModel, b: ContactModel) => {
				const x = a.vcard[1].reduce((prev: any, cx: any) => cx[0].localeCompare("fn") ? prev : cx[3], "");
				const y = b.vcard[1].reduce((prev: any, cx: any) => cx[0].localeCompare("fn") ? prev : cx[3], "");
				return x.localeCompare(y);
			}));
		});
	}, [form]);

	return (<>
		<Row className="mx-0">{
			contacts.map((contact: any, index: number) => (
				<ContactView
					key={index}
					index={index}
					contact={contact}
					form={form}
					onChange={onChange}
					doUpdate={doUpdate}
				/>
			))
		}</Row>
	</>);
}
function ModeZero(props: any) {
	const form = props.form as ContactForm;
	const { onChange } = props;

	const columnDefs = useMemo(() => store.columnDefs([]), [form, onChange]);
	const gridRef = useRef<AgGridReact>();
	const [contacts, setContacts] = useState<ContactModel[]>([]);

	useEffect(() => {
		store.search(form, (_: any, result: ContactModel[]) => {
			setContacts(result);
		});
	}, []);

	function handleOnGridReady() {
		gridRef && gridRef.current && gridRef.current.api && gridRef.current.api.sizeColumnsToFit();
		gridRef && gridRef.current && gridRef.current.api && gridRef.current.api.setDomLayout("autoHeight");
	}

	//console.log(form, contacts);
	return (<>
		<AgGridReact
			className="ag-theme-balham-dark"
			ref={gridRef}
			rowData={contacts}
			columnDefs={columnDefs}
			defaultColDef={{
				editable: true,
				sortable: true,
				resizable: true,
				suppressMenu: true,
			}}
			onGridReady={handleOnGridReady}
			stopEditingWhenCellsLoseFocus={true}
		/>
	</>);
}
function Upload(props: any) {
	const form = props.form as ContactForm;
	const { onChange } = props;

	function handleOnSubmit(file: any) {
		onChange && onChange({ map: new Map() });
		store.upload(file, (_: any, result: any) => onChange && onChange({ map: result }));
	}

	return (<>
		<Modal show={form.showUploadModal} size="xl" fullscreen={'xxl-down'} centered>
			<Modal.Header>
				<Modal.Title>연락처 올리기</Modal.Title>
				<CloseButton onClick={() => onChange && onChange({ showUploadModal: false })} />
			</Modal.Header>
			<Modal.Body>
				<DragAndDropFile
					onSubmit={handleOnSubmit}
					onDrop={handleOnSubmit}
					types={["application/json"]}
				/>
				<UploadResult
					form={form}
					onChange={onChange}
				/>
			</Modal.Body>
			<Modal.Footer>
				<Button variant="primary" size="lg" onClick={() => onChange && onChange({ showUploadModal: false })}>Close</Button>
			</Modal.Footer>
		</Modal>
	</>);
}
function UploadResult(props: any) {
	const form = props.form as ContactForm;
	const { onChange } = props;

	const [creates, setCreates] = useState([]);
	const [updates, setUpdates] = useState([]);
	const [removes, setRemoves] = useState([]);
	const [dups, setDups] = useState([]);

	function asort(a: any, b: any) {
		return a["content"].localeCompare(b["content"]);
	}

	useEffect(() => {
		if (!form.map) {
			setCreates([]);
			setDups([]);
			setUpdates([]);
			setRemoves([]);
			return;
		}

		form.map.creates && setCreates(form.map.creates.sort((a: any, b: any) => asort(a, b)));
		form.map.duplicates && setDups(form.map.duplicates.sort((a: any, b: any) => asort(a, b)));
		form.map.updates && setUpdates(form.map.updates.sort((a: any, b: any) => asort(a, b)));
		form.map.removes && setRemoves(form.map.removes.sort((a: any, b: any) => asort(a, b)));
	}, [form]);

	return (<>
		<Accordion defaultActiveKey="create" className="p-0">
			{
				(!creates || !creates.length) ? (
					<Row className="mx-1 p-2 bg-dark text-white">No Create Data!</Row>
				) : (
					<Accordion.Item eventKey={"create"}>
						<Accordion.Header>Create #{creates.length}</Accordion.Header>
						<Accordion.Body className="p-0 bg-dark text-white">
							<UploadCreate
								creates={creates}
								onChange={onChange}
							/>
						</Accordion.Body>
					</Accordion.Item>
				)
			}
			{
				(!removes || !removes.length) ? (
					<Row className="mx-1 p-2 bg-dark text-white">No Remove Data!</Row>
				) : (
					<Accordion.Item eventKey={"remove"}>
						<Accordion.Header>Remove #{removes.length}</Accordion.Header>
						<Accordion.Body className="p-0 bg-dark text-white">
							<UploadRemove
								removes={removes}
								onChange={onChange}
							/>
						</Accordion.Body>
					</Accordion.Item>
				)
			}
			{
				(!updates || !updates.length) ? (
					<Row className="mx-1 p-2 bg-dark text-white">No Update Data!</Row>
				) : (
					<Accordion.Item eventKey={"update"}>
						<Accordion.Header>Update #{updates.length}</Accordion.Header>
						<Accordion.Body className="p-0 bg-dark text-white">
							<UploadUpdate
								updates={updates}
								onChange={onChange}
							/>
						</Accordion.Body>
					</Accordion.Item>
				)
			}
			{
				(!dups || !dups.length) ? (
					<Row className="mx-1 p-2 bg-dark text-white">No Identical Data!</Row>
				) : (
					<Accordion.Item eventKey={"read"}>
						<Accordion.Header>Just Read #{dups.length}</Accordion.Header>
						<Accordion.Body className="p-0 bg-dark text-white">
							<UploadDuplicate
								dups={dups}
							/>
						</Accordion.Body>
					</Accordion.Item>
				)
			}
		</Accordion>
	</>);
}
function UploadCreate(props: any) {
	const { creates, onChange } = props;

	function handleOnClickDo(nodes: any) {
		nodes.forEach((node: any) => {
			store.create(node.data, () => {
				node.setSelected(false);
				node.selectable = false;
				onChange && onChange(node.data);
			});
		});
	}
	function handleOnClickDoBatch(nodes: any) {
		const creates = nodes.map((node: any) => node.data);
		store.batch({ creates: creates }, () => {
			nodes.forEach((node: any) => {
				node.setSelected(false);
				node.selectable = false;
				onChange && onChange(node.data);
			});
		});
	}

	return (
		<Common
			events={creates}
			onClickDo={handleOnClickDo}
			onClickDoBatch={handleOnClickDoBatch}
		/>
	);
}
function UploadUpdate(props: any) {
	const { updates, onChange } = props;

	function handleOnClickDo(nodes: any) {
		nodes.forEach((node: any) => {
			store.update(node.data, () => {
				node.setSelected(false);
				node.selectable = false;
				onChange && onChange(node.data);
			});
		});
	}
	function handleOnClickDoBatch(nodes: any) {
		const updates = nodes.map((node: any) => node.data);
		store.batch({ updates: updates }, () => {
			nodes.forEach((node: any) => {
				node.setSelected(false);
				node.selectable = false;
				onChange && onChange(node.data);
			});
		});
	}

	return (
		<Common
			events={updates}
			onClickDo={handleOnClickDo}
			onClickDoBatch={handleOnClickDoBatch}
		/>
	);
}
function UploadRemove(props: any) {
	const { removes, onChange } = props;

	function handleOnClickDo(nodes: any) {
		nodes.forEach((node: any) => {
			store.remove(node.data, () => {
				node.setSelected(false);
				node.selectable = false;
				onChange && onChange(node.data);
			});
		});
	}
	function handleOnClickDoBatch(nodes: any) {
		const removes = nodes.map((node: any) => node.data);
		store.batch({ removes: removes }, () => {
			nodes.forEach((node: any) => {
				node.setSelected(false);
				node.selectable = false;
				onChange && onChange(node.data);
			});
		});
	}

	return (
		<Common
			events={removes}
			onClickDo={handleOnClickDo}
			onClickDoBatch={handleOnClickDoBatch}
		/>
	);
}
function UploadDuplicate(props: any) {
	const { dups, onChange } = props;

	return (
		<Common
			events={dups}
			onChange={onChange}
		/>
	);
}
function Common(props: any) {
	const { events, onClickDo, onClickDoBatch } = props;
	const gridRef = useRef<AgGridReact>();
	const [rowData, setRowData] = useState([]);
	const [columnDefs, setColumnDefs] = useState([]);

	useEffect(() => {
		const comlumDefs = store.columnDefs();
		comlumDefs.push({
			field: "",
			headerName: "▦",
			maxWidth: 64,
			cellStyle: {
				textAlign: "center",
			},
			checkboxSelection: true,
			sortable: false,
		});
		setColumnDefs(comlumDefs);
	}, []);
	useEffect(() => {
		if (!events) {
			return;
		}
		setRowData(events);
		return function() { setRowData([]); };
	}, [events]);

	function handleOnClickDo() {
		const nodes = gridRef.current.api.getSelectedNodes();
		onClickDo(nodes);
	}
	function handleOnClickDoBatch() {
		const nodes = gridRef.current.api.getSelectedNodes();
		onClickDoBatch(nodes);
	}
	function handleOnClickToggleSelectAll() {
		const nodes = gridRef.current.api.getSelectedNodes();
		nodes.length ? gridRef.current.api.deselectAll() : gridRef.current.api.selectAll();
	}
	function handleOnClickSelectAllAndDoBatch() {
		gridRef.current.api.selectAll();
		handleOnClickDoBatch();
	}
	function handleOnGridReady() {
		gridRef && gridRef.current && gridRef.current.api && gridRef.current.api.sizeColumnsToFit();
		gridRef && gridRef.current && gridRef.current.api && gridRef.current.api.setDomLayout("autoHeight");
	}

	return (<>
		<Row className="mx-1 my-1">
			<Col className="me-auto">
				{onClickDo && (<Button variant="primary" size="sm" className="mx-2" onClick={handleOnClickDo}>Do</Button>)}
				{onClickDoBatch && (<Button variant="primary" size="sm" className="mx-2 me-auto" onClick={handleOnClickDoBatch}>Do Batch</Button>)}
				{onClickDoBatch && (<>
					<Button variant="primary" size="sm" className="mx-1" onClick={handleOnClickDoBatch}>Do Batch</Button>
					<Button variant="outline-warning" size="sm" className="mx-4" onClick={handleOnClickSelectAllAndDoBatch}>Select All And Do Batch</Button>
				</>)}
			</Col>
			<Col xs="auto">
				<Button variant="primary" size="sm" onClick={handleOnClickToggleSelectAll}>Toggle Select All</Button>
			</Col>
		</Row>
		<AgGridReact
			className="ag-theme-balham-dark"
			ref={gridRef}
			rowData={rowData}
			columnDefs={columnDefs}
			defaultColDef={{
				editable: true,
				sortable: true,
				resizable: true,
				suppressMenu: true,
			}}
			rowDragManaged={true}
			rowSelection="multiple"
			onGridReady={handleOnGridReady}
		/>
	</>);
}
function Header(props: any) {
	const form = props.form as ContactForm;
	const { onChange } = props;

	const inputRef = useRef(null);

	function handleOnClickDownload() {
		const yyyymmdd = moment().format("YYYYMMDD");
		onChange && onChange({ ...form, working: [...form.working, "download contact by json"], });
		store.download(`list-contact-${yyyymmdd}.json`,
			() => onChange && onChange({
				working: [...form.working.filter((work: any) => work !== "download contact by json")],
			})
		);
		onChange && onChange({ ...form, working: [...form.working, "download contact by vcf"], });
		store.downloadVcard({ filename: `list-contact-${yyyymmdd}.vcf`, priority: form.cutline },
			() => onChange && onChange({
				working: [...form.working.filter((work: any) => work !== "download contact by vcf")],
			})
		);
	}
	function handleOnKeyPressKeyword(event: any) {
		if (event.key === 'Enter') {
			onChange && onChange({ keyword: event.target.value, });
		}
	}
	function handleOnClickSearch() {
		inputRef.current && onChange && onChange({ keyword: inputRef.current.value, });
	}

	return (<>
		<Row className="mx-0 py-1 bg-dark border-top border-secondary">
			<Col xs="auto" className="px-1 me-auto">
				<InputGroup>
					<Priority
						title={JSON.stringify(form)}
						min="0"
						max="16"
						value={form.cutline}
						onChange={(e: any) => onChange && onChange({ cutline: Number(e.target.value) })}
					/>
					<Col xs="auto" className="ms-4">
						<Form.Control size="sm" type="search" className="bg-dark text-white" defaultValue={form.keyword}
							ref={inputRef}
							onKeyPress={handleOnKeyPressKeyword}
						/>
					</Col>
					<Col xs="auto" className="ms-1">
						<Button size="sm" variant="secondary" className="ms-1" title={form.mode.toString()} onClick={handleOnClickSearch}>찾기</Button>
					</Col>
				</InputGroup>
			</Col>
			<Col xs="auto" className="">
				<InputGroup size="sm">
					<Button size="sm" variant="secondary" className="ms-1" title={form.mode.toString()} onClick={(_: any) => onChange && onChange({ showUploadModal: !form.showUploadModal })}>올리기</Button>
					<Button size="sm" variant="secondary" className="ms-1" title={form.mode.toString()} onClick={handleOnClickDownload}>다운로드</Button>
					<Button size="sm" variant="secondary" className="ms-1" title={form.mode.toString()} onClick={(_: any) => onChange && onChange({ mode: form.mode + 1 })}>모드</Button>
				</InputGroup>
			</Col>
		</Row>
	</>);
}
