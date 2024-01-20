import React, { useState } from 'react';
import { Button, Form, Row, Col } from 'react-bootstrap';
import moment from 'moment';
import { useDrop } from 'react-dnd';
import { NativeTypes } from 'react-dnd-html5-backend';

// DragAndDropFile.tsx
export default ((props: any) => {
	const { types, onSubmit } = props;

	const [selectedFile, setSelectedFile] = useState({ name: "", type: "", size: 0, lastModifiedDate: "" });
	const [isFilePicked, setIsFilePicked] = useState(false);

	function changeHandler(event: any) {
		setSelectedFile(event.target.files[0]);
		setIsFilePicked(true);
	}

	const [{ isOver, canDrop }, drop] = useDrop(
		() => ({
			accept: [NativeTypes.FILE],
			drop: (item: any) => {
				setSelectedFile(item.files[0]);
				setIsFilePicked(true);
				onSubmit(item.files[0]);
			},
			canDrop: (item: any) => {
				let acceptType = true;
				types.forEach((item: any) => acceptType = acceptType || item);
				const possible = item.items.length === 1
					&& item.items[0].kind === 'file'
					&& acceptType
					;
				return possible
			},
			collect: (monitor) => ({
				isOver: !!monitor.isOver(),
				canDrop: !!monitor.canDrop(),
			}),
		})
	);

	const isActive = canDrop && isOver;
	let clazz = "p-4 text-start py-0";
	if (isOver && canDrop) {
		clazz += " bg-danger";
	} else if (isOver && !canDrop) {
		clazz += " bg-success";
	}

	return (<>
		<Form ref={drop} className={clazz}>
			<Form.Group as={Row} className="m-1 py-0 ">
				<Form.Label column sm={3} className="text-end ">file</Form.Label>
				<Col sm={9}><Form.Control size="sm" type="file" name="file" className="bg-black text-white" onChange={changeHandler} /></Col>
			</Form.Group>
			{isFilePicked ? (
				<>
					<Form.Group as={Row} className="m-1">
						<Form.Label column sm={3} className="text-end">Filename</Form.Label>
						<Col sm={9}><Form.Control size="sm" type="text" className="bg-black text-white" value={selectedFile.name} readOnly /></Col>
					</Form.Group>
					<Form.Group as={Row} className="m-1">
						<Form.Label column sm={3} className="text-end">Filetype</Form.Label>
						<Col sm={9}><Form.Control size="sm" type="text" className="bg-black text-white" value={selectedFile.type} readOnly /></Col>
					</Form.Group>
					<Form.Group as={Row} className="m-1">
						<Form.Label column sm={3} className="text-end">Size in bytes</Form.Label>
						<Col sm={9}><Form.Control size="sm" type="text" className="bg-black text-white" value={selectedFile.size.toLocaleString()} readOnly /></Col>
					</Form.Group>
					<Form.Group as={Row} className="m-1">
						<Form.Label column sm={3} className="text-end">lastModifiedDate</Form.Label>
						<Col sm={9}>
							<Form.Control size="sm" type="text" className="bg-black text-white" value={
								moment(selectedFile.lastModifiedDate).format("YYYY-MM-DD HH:mm:ss")
							} readOnly />
						</Col>
					</Form.Group>
				</>
			) : (
				<Form.Group as={Row} className="m-1">
					<Form.Label column sm={3} className="text-end"></Form.Label>
					<Col sm={9}>Select a file to show details. {isActive ? 'Release to drop' : 'Drag file here'}</Col>
				</Form.Group>
			)}
			<Form.Group as={Row} className="m-1">
				<Form.Label column sm={3} className="text-end"></Form.Label>
				<Col sm={9}>
					<Button variant="primary" size="sm" onClick={() => onSubmit(selectedFile)}>Submit</Button>
				</Col>
			</Form.Group>
		</Form>
	</>);
});
