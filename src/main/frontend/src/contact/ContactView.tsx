import moment from "moment";
import React, { useEffect, useState } from "react";
import { Badge, Button, CloseButton, Col, Form, InputGroup, Modal, Row, ToggleButton, ToggleButtonGroup } from "react-bootstrap";

import AppStore from "../AppStore";

// domain
import ContactForm from "./ContactForm";
import ContactModel, { ContactMapModel } from "./ContactModel";
import store from "./ContactStore";

// store

// constant
const hides = ["version", "prodid", "x-ablabel"];
const editable = ["adr", "note", "tel"];
const rangeStyle = {
	background: "linear-gradient(to right, rgb(0, 0, 0) 22%, rgb(0, 64, 0) 28% 47% , rgb(64, 64, 0) 53% 72% , rgb(64, 0, 0) 78% 97%, rgb(128, 0, 0) 99%)",

};
// ContactView.tsx
export default ((props: any) => {
	AppStore.heartbeat();

	const contact = props.contact as ContactModel;
	const form = props.form as ContactForm;
	const { index, onChange } = props;

	const [map, setMap] = useState<Map<string, ContactMapModel>>(new Map<string, ContactMapModel>());

	useEffect(() => {
		if (!contact?.maps) {
			return;
		}

		const maps: Map<string, ContactMapModel> = contact.maps
			.reduce((prev: Map<string, ContactMapModel>, current: ContactMapModel) => prev.set(current.key, current), new Map<string, ContactMapModel>());
		setMap(maps);
	}, [contact]);

	function sortField(a: any[], b: any[]): number {
		const priority = {
			fn: 1,
			n: 2,
			nickname: 3,
			email: 4,
			tel: 5,
			adr: 6,
		};

		const nameA: string = a[0] || "";
		const nameB: string = b[0] || "";

		const priorityA = priority[nameA] || Object.getOwnPropertyNames(priority).length + 8;
		const priorityB = priority[nameB] || Object.getOwnPropertyNames(priority).length + 8;
		const priorityCompared = priorityA - priorityB;
		if (priorityCompared != 0) {
			return priorityCompared;
		}

		const nameCompared = nameA.localeCompare(nameB);
		if (nameCompared != 0) {
			return nameCompared;
		}

		const mapKeyA = `${contact.id}.${a[0]}:${JSON.stringify(a)}`;
		const mapKeyB = `${contact.id}.${b[0]}:${JSON.stringify(b)}`;
		const mapValueA = map.get(mapKeyA);
		const mapValueB = map.get(mapKeyB);
		const userPriorityA = mapValueA?.value || 0;
		const userPriorityB = mapValueB?.value || 0;
		const userPriorityCompared = userPriorityA - userPriorityB;

		if (userPriorityCompared) {
			return userPriorityCompared;
		}

		return mapKeyA.localeCompare(mapKeyB);
	}

	const key = `${contact.id}`;
	const data = map.get(key);
	if (data && Number.isInteger(data.value) && data.value > form.cutline) {
		return (<></>);
	}
	const json = JSON.stringify(contact);
	if (form.keyword.length > 0 && !json.includes(form.keyword)) {
		return (<></>);
	}

	return (<>
		<Col xs="3" className="px-3 py-1 border-bottom text-start bg-black text-white">
			<Badge bg={store.bgByPriority(data?.value)} className="me-1" title={JSON.stringify(contact)}>{index}</Badge>
			{contact.vcard[1].sort((a: any, b: any) => sortField(a, b)).map((child: any) => (
				<General
					key={Math.random()}
					id={contact.id}
					data={child}
					map={map}
					form={form}
					onChange={onChange}
				/>
			))}
			<Button size="sm" variant="outline-primary" className="mx-1 py-0" onClick={() => onChange && onChange({ updating: contact })} >
				수정
			</Button>
		</Col>
	</>);
});

export function Priority(props: any) {
	const { size, type, xs, ...properties } = props;

	return (<>
		<Row className="mx-0 px-0">
			<Col className="me-0 pe-0">
				<Form.Range
					{...properties}
					style={{
						background: "linear-gradient(to right, rgb(0, 0, 0) 22%, rgb(0, 64, 0) 28% 47% , rgb(64, 64, 0) 53% 72% , rgb(64, 0, 0) 78% 97%, rgb(128, 0, 0) 99%)",
						marginTop: "0.25rem",
					}}
				/>
			</Col>
			<Col xs="auto" className="ms-0 ps-0">
				<Form.Control
					size="sm"
					type="number"
					style={{
						color: store.colorByPriority(properties.value),
						width: 64,
						marginLeft: 2,
						background: "transparent",
					}}
					{...properties}
				/>
			</Col>
		</Row>
	</>);
}

export function UpdateModal(props: any) {
	const contact = props.contact as ContactModel;
	const form = props.form as ContactForm;
	const { onClose, onUpdate } = props;

	const [refresh, setRefresh] = useState(false);
	const [clone, setClone] = useState<ContactModel>();
	const [map, setMap] = useState<Map<string, ContactMapModel>>(new Map<string, ContactMapModel>());

	useEffect(() => {
		if (!contact) {
			setClone(null);
			setMap(new Map());
			return;
		}

		const copied: ContactModel = JSON.parse(JSON.stringify(contact));
		setClone(copied);
		const maps: Map<string, ContactMapModel> = copied.maps.reduce((prev: Map<string, ContactMapModel>, current: ContactMapModel) =>
			prev.set(current.key, current)
			, new Map<string, ContactMapModel>());
		setMap(maps);
	}, [contact]);

	function handleOnClickUpdate() {
		clone.maps = Array.from(map.values());
		onUpdate && onUpdate(clone);
	}
	function onChangeContactMapValue(key: string, value: number) {
		let cmap: ContactMapModel = map.get(key);
		if (!cmap) {
			cmap = {
				vcardId: clone.id,
				key: key,
				value: value,
			};
			map.set(key, cmap);
		}
		cmap.value = value;
		setRefresh(!refresh);
	}

	if (!clone) {
		return (<></>);
	}

	const title = clone.vcard[1].find((cx: any) => !cx[0].localeCompare("fn"));
	const classNameReadOnly = "bg-dark text-light";

	return (<>
		<Modal show={!!clone} size="lg" centered>
			<Modal.Header className="bg-black text-white border border-secondary">
				<Modal.Title title={JSON.stringify(contact)}>
					<Row>
						<Col xs="auto" className="px-1">
							{title && title[3]}
						</Col>
						<Col xs="auto" className="mx-1">
							<Button variant="primary" size="sm" className="mx-1" onClick={handleOnClickUpdate}>적용</Button>
							<Button variant="primary" size="sm" className="mx-1" onClick={onClose}>닫기</Button>
						</Col>
					</Row>
				</Modal.Title>
				<CloseButton className="bg-black text-white" onClick={onClose} />
			</Modal.Header>
			<Modal.Body className="bg-black text-white border border-secondary">
				<Form.Group as={Row} className="mb-3">
					<InputGroup size="sm" as={Col}>
						<InputGroup.Text className={classNameReadOnly}>id</InputGroup.Text>
						<Form.Control className={classNameReadOnly} value={clone.id} readOnly />
					</InputGroup>
					<InputGroup as={Col} size="sm">
						<InputGroup.Text className={classNameReadOnly}>수정일</InputGroup.Text>
						<Form.Control className={classNameReadOnly} readOnly value={moment(clone.updated).format("YYYY-MM-DD (dd)")} />
					</InputGroup>
					<InputGroup as={Col} size="sm">
						<InputGroup.Text className="bg-black text-white">생성일</InputGroup.Text>
						<Form.Control className="bg-black text-white" readOnly value={moment(clone.created).format("YYYY-MM-DD (dd)")} />
					</InputGroup>
					<Priority
						min="0"
						max="16"
						value={map.get(`${clone.id}`)?.value || 0}
						onChange={(e: any) => onChangeContactMapValue(`${clone.id}`, Number(e.target.value))}
					/>
				</Form.Group>
				{clone.vcard[1].filter((x: any[]) => !hides.includes(x[0])).map((x: any[], xindex: number) => {
					const key = `${clone.id}.${x[0]}:${JSON.stringify(x)}`;
					const className = editable.includes(x[0]) ? "bg-black text-white" : "bg-dark text-light";
					return (<FieldEditor
						key={`${form.cutline}.${xindex}`}
						keyMap={key}
						form={form}
						x={x}
						map={map}
						className={className}
						onUpdate={() => setRefresh(!refresh)}
						onChangeContactMapValue={onChangeContactMapValue}
						xindex={xindex}
					/>);
				})}
			</Modal.Body>
			<Modal.Footer className="bg-black text-white border border-secondary" style={{ maxHeight: 128 }}>
				<Button variant="primary" size="sm" onClick={handleOnClickUpdate}>적용</Button>
				<Button variant="primary" size="sm" onClick={onClose}>닫기</Button>
			</Modal.Footer>
		</Modal>
	</>);
}

function FieldTextEditor(props: any) {
	const className = props.className as string;
	const { onUpdate, name, data, index } = props;

	return (<>
		{data[index].search(/(\n|(\\n))/g) >= 0 ? (
			<textarea
				rows={5}
				value={data[index]}
				className={`${className} w-100`}
				readOnly={!editable.includes(name)}
				onChange={(e: any) => {
					data[index] = e.target.value;
					onUpdate && onUpdate(data);
				}}
			/>
		) : (
			<Form.Control
				value={data[index]}
				className={className}
				readOnly={!editable.includes(name)}
				onChange={(e: any) => {
					data[index] = e.target.value;
					onUpdate && onUpdate(data);
				}}
			/>
		)}
	</>);
}

function FieldEditor(props: any) {
	const { keyMap, x, map, onChangeContactMapValue, onUpdate } = props;

	const className = "bg-black text-success";

	if ("note".localeCompare(x[0]) == 0) {
		return (<>
			<Form.Group as={Row} className="mb-3">
				<InputGroup size="sm" as={Col}>
					<InputGroup.Text className={className} title={JSON.stringify(x)}>{x[0]}</InputGroup.Text>
					<FieldTextEditor
						className={className}
						onUpdate={onUpdate}
						name={x[0]}
						data={x}
						title={JSON.stringify(x)}
						index={3}
					/>
				</InputGroup>
				<Priority
					min="0"
					max="16"
					value={map.get(keyMap)?.value || 0}
					onChange={(e: any) => onChangeContactMapValue(keyMap, Number(e.target.value))}
				/>
			</Form.Group>
		</>);
	}

	return (<>
		<Form.Group as={Row} className="mb-3">
			<InputGroup size="sm">
				<InputGroup.Text className={className} title={JSON.stringify(x)}>{x[0]}</InputGroup.Text>
				<Field
					data={x}
				/>
			</InputGroup>
			<Priority
				min="0"
				max="16"
				value={map.get(keyMap)?.value || 0}
				onChange={(e: any) => onChangeContactMapValue(keyMap, Number(e.target.value))}
			/>
		</Form.Group>
	</>);
}
function Field(props: any) {
	const { data } = props;

	if (Array.isArray(data[3])) {
		return (<>
			<ToggleButtonGroup type="checkbox">
				{data[3].map((child: any) => (
					<ToggleButton
						id={Math.random().toString()}
						value={child}
						key={Math.random()}
						size="sm"
						variant="outline-secondary"
						className="py-0"
						title={JSON.stringify(data)}
						onClick={(_: any) => {
							navigator.clipboard.writeText(data[0]);
						}}
					>
						{child}
					</ToggleButton>
				))}
			</ToggleButtonGroup>
		</>);
	}

	if (data.length > 3) {
		return (<>
			<ToggleButtonGroup type="checkbox">
				{data.slice(3).map((child: any) => (
					<ToggleButton
						id={Math.random().toString()}
						value={child}
						key={Math.random()}
						size="sm"
						variant="outline-secondary"
						className="py-0"
						title={JSON.stringify(data)}
						onClick={(_: any) => {
							navigator.clipboard.writeText(data[0]);
						}}
					>
						{child}
					</ToggleButton>
				))}
			</ToggleButtonGroup>
		</>);
	}
	return (<>
		<Button
			key={Math.random()}
			size="sm"
			variant="outline-secondary"
			className="mx-1 py-0"
			title={JSON.stringify(data)}
			onClick={(_: any) => {
				navigator.clipboard.writeText(data[0]);
			}}
		>{data[3]}</Button>
	</>);

}

function General(props: any) {
	const form = props.form as ContactForm;
	const { id, data, map } = props;

	if (!data || !Array.isArray(data) || hides.includes(data[0])) {
		return (<></>);
	}

	const key = `${id}.${data[0]}:${JSON.stringify(data)}`;
	const value = map.get(key);
	if (value && value.value && value.value > form.cutline) {
		//console.log("cutline: ", data)
		return (<></>);
	}

	switch (data[0]) {
		// default
		case "fn":
		case "n":
		case "org":
		case "tel":
		case "categories":
		case "note":
		case "title":
		case "email":
		case "adr":
		case "x-abrelatednames":
		case "bday":
		case "nickname":
		case "url":
			if (data[0]) {
				return (<Field
					data={data}
				/>);
			}
			if (Array.isArray(data[3])) {
				return (<>
					<ToggleButtonGroup type="checkbox">
						{data[3].map((child: any) => (
							<ToggleButton
								id={Math.random().toString()}
								value={child}
								key={Math.random()}
								size="sm"
								variant="outline-secondary"
								className="py-0"
								title={JSON.stringify(data)}
								onClick={(_: any) => {
									navigator.clipboard.writeText(data[0]);
								}}
							>
								{child}
							</ToggleButton>
						))}
					</ToggleButtonGroup>
				</>);
			}

			if (data.length > 3) {
				return (<>
					<ToggleButtonGroup type="checkbox">
						{data.slice(3).map((child: any) => (
							<ToggleButton
								id={Math.random().toString()}
								value={child}
								key={Math.random()}
								size="sm"
								variant="outline-secondary"
								className="py-0"
								title={JSON.stringify(data)}
								onClick={(_: any) => {
									navigator.clipboard.writeText(data[0]);
								}}
							>
								{child}
							</ToggleButton>
						))}
					</ToggleButtonGroup>
				</>);
			}
			return (<>
				<Button
					key={Math.random()}
					size="sm"
					variant="outline-secondary"
					className="mx-1 py-0"
					title={JSON.stringify(data)}
					onClick={(_: any) => {
						navigator.clipboard.writeText(data[0]);
					}}
				>{data[3]}</Button>
			</>);

		// elips
		case "photo":
			let title: string = data[3].toString();
			if (title.length > 16) {
				title = `${data[3].toString().slice(0, 4)}…${data[3].toString().slice(-4)}`;
			}

			return (<>
				<Button
					key={Math.random()}
					size="sm"
					variant="outline-secondary"
					className="mx-1 py-0"
					title={JSON.stringify(data)}
					onClick={(_: any) => {
						navigator.clipboard.writeText(data[0]);
					}}
				>{title}</Button>
			</>);

		// hides. duplicated.
		//case "version":
		//case "prodid":
		//case "n":
		//case "x-ablabel":
		//return (<></>);
	}

	return (<>
		<Button
			key={Math.random()}
			size="sm"
			variant="secondary"
			className="mx-1 py-0"
			title={JSON.stringify(data)}
			onClick={(_: any) => {
				navigator.clipboard.writeText(data[0]);
			}}
		>▒ {data[3]}</Button>
	</>);
}
