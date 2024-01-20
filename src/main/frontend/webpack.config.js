import { join } from "path";
import HtmlWebpackPlugin from "html-webpack-plugin";

export const mode = process.env.NODE_ENV || "development";
export const entry = "./src/index.tsx";
export const resolve = {
	extensions: [".tsx", ".ts", "..."],
};
export const output = {
	filename: "bundle.js",
	path: join(__dirname, "/dist"),
};
export const devtool = "eval-source-map";
export const devServer = {
	port: 3000,
	open: true,
	hot: true,
};
export const module = {
	rules: [
		{
			test: /\.(ts|tsx)$/,
			exclude: /node_modules/,
			use: ["babel-loader", "ts-loader"],
		},
		{
			test: /\.(jpg|png|gif|svg)$/,
			use: ["file-loader"],
		},
	],
};
export const plugins = [
	new HtmlWebpackPlugin({
		template: join(__dirname, "public", "index.html"),
	}),
];
