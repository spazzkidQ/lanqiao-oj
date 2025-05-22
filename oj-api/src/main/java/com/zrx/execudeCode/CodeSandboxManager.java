package com.zrx.execudeCode;

import com.zrx.codesandbox.CodeSandBox;
import com.zrx.codesandbox.LocalCodeSandbox;
import com.zrx.remote.RemoteCodeSandbox;

public class CodeSandboxManager {

	public static CodeSandBox instance(String type) {
		if (type.equals("remote")) {
			return new RemoteCodeSandbox();  // 远程调用;
		}
		else if (type.equals("local")) {
			return new LocalCodeSandbox();  // 本地调用的接口;
		}
		else {
			return new RemoteCodeSandbox();
		}
	}

}
