package com.pinyougou.manager.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.web.util.RedirectUrlBuilder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import entity.Result;
import utils.FastDFSClient;


@RestController
@RequestMapping("/upload")
public class UploadController {
	
	@Value("${FILE_SERVER_URL}")
	private String file_server_url;

	@RequestMapping("/uploadFile")
	public Result uploadFile(MultipartFile file){
		String path = file.getOriginalFilename();//获取文件的绝对路径
		String extName = path.substring(path.lastIndexOf(".")+1);//截取文件名，获取扩展名
		try {
			FastDFSClient fastDFSClient = new FastDFSClient("classpath:config/fdfs_client.conf");//加载配置文件，获取工具类
			String uploadFile = fastDFSClient.uploadFile(file.getBytes(), extName, null);//文件上传 参数1：图片流信息
			return new Result(true,file_server_url+uploadFile);
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false,"上传操作失败");
		}
	}
}

/*@RestController
@RestController 
public class UploadController {
	
	@Value("${FILE_SERVER_URL}")
	private String FILE_SERVER_URL;
	
	@RequestMapping("/upload")
	public Result upload(MultipartFile file){
		//获取文件的扩展名;
		String originalFilename = file.getOriginalFilename();
		String extName = originalFilename.substring(originalFilename.lastIndexOf("."));
		try {
			//创建一个FastDFS的客户端;
			FastDFSClient dfsClient = new FastDFSClient("classpath:config/fdfs_client.conf");
			//执行上传处理;
			String path = dfsClient.uploadFile(file.getBytes(), extName);
			//拼接返回的url和IP地址;得到完整的url;
			String url = FILE_SERVER_URL+path;
			//返回结果;
			return new Result(true, url);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return new Result(false,"上传失败");
		}
	}
}*/
