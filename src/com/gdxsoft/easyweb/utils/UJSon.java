package com.gdxsoft.easyweb.utils;


import org.json.JSONObject;

public class UJSon {

	/**
	 * 创建一个 错误返回结果 json
	 * 
	 * @param err
	 * @return
	 */
	public static JSONObject rstFalse(String err) {
		JSONObject rst = new JSONObject();
		rstSetFalse(rst, err);
		return rst;
	}

	/**
	 * 创建一个 正确返回结果 json
	 * 
	 * @param msg
	 * @return
	 */
	public static JSONObject rstTrue(String msg) {
		JSONObject rst = new JSONObject();
		rstSetTrue(rst, msg);
		return rst;
	}

	/**
	 * 设置返回结果 true
	 * 
	 * @param rst
	 * @param msg
	 */
	public static void rstSetTrue(JSONObject rst, String msg) {
		rst.put("RST", true);
		rst.put("MSG", msg);
	}

	/**
	 * 设置返回结果 false
	 * 
	 * @param rst
	 * @param err
	 */
	public static void rstSetFalse(JSONObject rst, String err) {
		rst.put("RST", false);
		rst.put("ERR", err);
	}

}
