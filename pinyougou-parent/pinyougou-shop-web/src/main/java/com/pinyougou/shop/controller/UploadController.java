package com.pinyougou.shop.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import entity.Result;
import utils.FastDFSClient;

@RestController
@RequestMapping("/upload")
public class UploadController {
	
	@Value("${FILE_SERVER_URL}")
	private String FILE_SERVER_URL;
	
	@RequestMapping("/uploadFile")
	public Result upload(MultipartFile file){
		/*//获取文件的扩展名;
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
		}*/
		
		//1、取文件的扩展名
		System.out.println(file);
		String originalFilename = file.getOriginalFilename();
		String extName = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
		try {
		//2、创建一个 FastDFS 的客户端
			FastDFSClient fastDFSClient = new FastDFSClient("classpath:config/fdfs_client.conf");
			//3、执行上传处理
			System.out.println(extName);
			String path = fastDFSClient.uploadFile(file.getBytes(), extName,null);
			//4、拼接返回的 url 和 ip 地址，拼装成完整的 url
			String url = FILE_SERVER_URL + path;	
			System.out.println(url);
			return new Result(true,url);			
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "上传失败");
		}		
	}
}
