package com.xuecheng.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.api.R;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import com.xuecheng.base.execption.ValidationGroups;
import com.xuecheng.base.execption.XueChengPlusException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.base.model.RestResponse;
import com.xuecheng.media.mapper.MediaFilesMapper;
import com.xuecheng.media.mapper.MediaProcessMapper;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFileParamsDto;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.service.MediaFileService;
import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.DeleteObject;
import jdk.internal.org.jline.utils.Log;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.Streams;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.util.ExceptionTypeFilter;

import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @description TODO
 * @author Mr.M
 * @date 2022/9/10 8:58
 * @version 1.0
 */
@Slf4j
 @Service
public class MediaFileServiceImpl implements MediaFileService {

  @Autowired
 MediaFilesMapper mediaFilesMapper;

  @Autowired
 MediaProcessMapper mediaProcessMapper;

  @Autowired
 MinioClient minioClient;

  @Autowired
  MediaFileService currentProxy;
 //普通文件桶
 @Value("${minio.bucket.files}")
 private String bucket_Files;
 //视频桶
 @Value("${minio.bucket.videofiles}")
 private String bucket_video;
 @Override
 public PageResult<MediaFiles> queryMediaFiels(Long companyId,PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto) {

  //构建查询条件对象
  LambdaQueryWrapper<MediaFiles> queryWrapper = new LambdaQueryWrapper<>();
  
  //分页对象
  Page<MediaFiles> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
  // 查询数据内容获得结果
  Page<MediaFiles> pageResult = mediaFilesMapper.selectPage(page, queryWrapper);
  // 获取数据列表
  List<MediaFiles> list = pageResult.getRecords();
  // 获取数据总数
  long total = pageResult.getTotal();
  // 构建结果集
  PageResult<MediaFiles> mediaListResult = new PageResult<>(list, total, pageParams.getPageNo(), pageParams.getPageSize());
  return mediaListResult;

 }

 private String getMimeType(String extension){
  if (extension==null){
   extension="";
  }
  //根据扩展名取出mimetype
  ContentInfo extensionMatch= ContentInfoUtil.findExtensionMatch(extension);
  //通用mimetype字节流
  String mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
  if (extensionMatch!=null){
   mimeType=extensionMatch.getMimeType();
  }
  return mimeType;
 }

 /**
  * 将文件写入minio
  * @param localFilePath
  * @param mimeType
  * @param bucket
  * @param objectName
  * @return
  */
 public boolean addMediaFilesToMinIO(String localFilePath,String mimeType,String bucket, String objectName){
  try {
   UploadObjectArgs testbucket= UploadObjectArgs.builder()
           .bucket(bucket)
           .object(objectName)
           .filename(localFilePath)
           .contentType(mimeType)
           .build();
   minioClient.uploadObject(testbucket);
   log.debug("上传文件到minio成功,bucket:{},objectName:{}",bucket,objectName);
   System.out.println("上传成功");
   return true;
  }catch (Exception e){
   e.printStackTrace();
   log.error("上传文件到minio出错,bucket:{},objectName:{},错误原因:{}",bucket,objectName,e.getMessage(),e);
   XueChengPlusException.cast("上传文件到文件系统失败");
  }
  return false;
 }

 @Override
 public MediaFiles getFileById(String mediaId) {
  MediaFiles mediaFiles = mediaFilesMapper.selectById(mediaId);
  return mediaFiles;
 }

 //获取文件默认存储目录路径 年/月/日
 private String getDefaultFolderPath(){
  SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
  String folder=sdf.format(new Date()).replace("-","/")+"/";
  return folder;
 }

 //获取文件的md5
 private String getFileMd5(File file){
  try (FileInputStream fileInputStream=new FileInputStream(file)){
   String fileMd5= DigestUtils.md5DigestAsHex(fileInputStream);
   return fileMd5;
  }catch (Exception e){
   e.printStackTrace();
   return null;
  }
 }

 /**
  * 将文件信息添加到文件表
  * @param companyId
  * @param fileMd5
  * @param uploadFileParamsDto
  * @param bucket
  * @param objectName
  * @return
  */
 @Transactional
 public MediaFiles addMediaFilesToDb(Long companyId,String fileMd5,UploadFileParamsDto uploadFileParamsDto,String bucket,String objectName){
  //从数据库查询文件
  MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
  if (mediaFiles == null) {
   mediaFiles = new MediaFiles();
   //拷贝基本信息
   BeanUtils.copyProperties(uploadFileParamsDto, mediaFiles);
   mediaFiles.setId(fileMd5);
   mediaFiles.setFileId(fileMd5);
   mediaFiles.setCompanyId(companyId);
   mediaFiles.setUrl("/" + bucket + "/" + objectName);
   mediaFiles.setBucket(bucket);
   mediaFiles.setFilePath(objectName);
   mediaFiles.setCreateDate(LocalDateTime.now());
   mediaFiles.setAuditStatus("002003");
   mediaFiles.setStatus("1");
   //保存文件信息到文件表
   int insert = mediaFilesMapper.insert(mediaFiles);
   if (insert < 0) {
    log.error("保存文件信息到数据库失败,{}", mediaFiles);
    XueChengPlusException.cast("保存文件信息失败");
   }
   //添加到待处理任务表中
   addWaitingTask(mediaFiles);
   log.debug("保存文件信息到数据库成功,{}", mediaFiles);

  }
  return mediaFiles;
 }

 @Override
 public RestResponse<Boolean> checkFile(String fileMd5) {
  //先查询数据库
  MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
  if (mediaFiles!=null) {
   //桶
   String bucket = mediaFiles.getBucket();
   //objectname
   String filePath = mediaFiles.getFilePath();
   //如果数据库存在再查询minio
   GetObjectArgs getObjectArgs = GetObjectArgs.builder()
           .bucket(bucket)
           .object(filePath)
           .build();
   //查询远程服务获取到一个流对象
   try {
    FilterInputStream inputStream = minioClient.getObject(getObjectArgs);
    if (inputStream!=null){
     //文件存在
     return RestResponse.success(true);
    }
   } catch (Exception e) {
    e.printStackTrace();
   }
  }
  //文件不存在
  return RestResponse.success(false);
 }

 @Override
 public RestResponse<Boolean> checkChunk(String fileMd5, int chunkIndex) {
  //分块存储路径是：md5前两位为两个目录，chunk存储分块文件
  //根据md5得到分块路径
  String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
  //如果数据库存在再查询minio
  GetObjectArgs getObjectArgs = GetObjectArgs.builder()
          .bucket(bucket_video)
          .object(chunkFileFolderPath+chunkIndex)
          .build();
  //查询远程服务获取到一个流对象
  try {
   FilterInputStream inputStream = minioClient.getObject(getObjectArgs);
   if (inputStream!=null){
    //文件存在
    return RestResponse.success(true);
   }
  } catch (Exception e) {
   e.printStackTrace();
  }
  //文件不存在
  return RestResponse.success(false);
 }

 @Override
 public RestResponse uploadChunk(String fileMd5, int chunk, String localChunkFilePath) {
  //分块文件的路径
  String chunkFileFolderPath = getChunkFileFolderPath(fileMd5)+chunk;
  //获取mimeType
  String mimeType = getMimeType(null);
  //将分块文件上传到minio
  boolean b = addMediaFilesToMinIO(localChunkFilePath, mimeType, bucket_video, chunkFileFolderPath);
  if (!b){
   return RestResponse.validfail(false,"上传分块文件失败");
  }
  //上传成功
  return RestResponse.success(true);
 }

 @Override
 public RestResponse mergechunks(Long companyId, String fileMd5, int chunkTotal, UploadFileParamsDto uploadFileParamsDto) {
  //分块文件目录
  String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
  //找到分块文件调用minio的sdk进行文件合并
  List<ComposeSource> sources= Stream.iterate(0,i->++i).limit(chunkTotal)
          .map(i->ComposeSource.builder().bucket(bucket_video).object(chunkFileFolderPath+i).build()).collect(Collectors.toList());
  //源文件名称
  String filename = uploadFileParamsDto.getFilename();
  //扩展名
  String extension = filename.substring(filename.lastIndexOf("."));
  //合并文件后的objectname
  String objectName = getFilePathByMd5(fileMd5, extension);
  //指定合并后的objectname信息
  ComposeObjectArgs composeObjectArgs=ComposeObjectArgs.builder()
          .bucket(bucket_video)
          .object(objectName)
          .sources(sources)
          .build();
  try {
   minioClient.composeObject(composeObjectArgs);
  }catch (Exception e){
   e.printStackTrace();
   log.error("合并文件出错，bucket:{},objectName:{},错误信息:{}",bucket_video,objectName,e.getMessage());
   return RestResponse.validfail(false,"合并文件异常");
  }

  //校验合并后的和源文件是否一致，视频上传才成功
  //先下载合并后的文件
  File file = downloadFileFromMinIO(bucket_video, objectName);
  try(FileInputStream fileInputStream=new FileInputStream(file)){
   //计算合并后文件的md5
   String mergeFile_md5 = DigestUtils.md5DigestAsHex(fileInputStream);
   //比较原始的md5值和合并后文件的md5
   if (!fileMd5.equals(mergeFile_md5)){
    log.error("校验合并文件md5值不一致，原始文件：{},合并文件:{}",fileMd5,mergeFile_md5);
    return RestResponse.validfail(false,"文件校验失败");
   }
   //文件大小
   uploadFileParamsDto.setFileSize(file.length());
  }catch (Exception e){
   return RestResponse.validfail(false,"文件校验失败");
  }
  //将文件信息入库
  MediaFiles mediaFiles = currentProxy.addMediaFilesToDb(companyId, fileMd5, uploadFileParamsDto, bucket_video, objectName);
  if (mediaFiles==null){
   return RestResponse.validfail(false,"文件入库失败");
  }
  //清理分块文件
  clearChunkFiles(chunkFileFolderPath,chunkTotal);
  return RestResponse.success(true);
 }

 @Override
 public UploadFileResultDto uploadFile(Long companyId, UploadFileParamsDto uploadFileParamsDto, String localFilePath,String objectName) {

  File file=new File(localFilePath);
  if (!file.exists()){
   XueChengPlusException.cast("文件不存在");
  }
  //文件名
  String filename= uploadFileParamsDto.getFilename();
  //先得到扩展名
  String extension=filename.substring(filename.lastIndexOf("."));
  //得到mimetype
  String mimetype=getMimeType(extension);
  //子目录
  String defaultFolderPath=getDefaultFolderPath();
  //文件的md5值
  String fileMd5=getFileMd5(file);
  if(StringUtils.isEmpty(objectName)){
   objectName =  defaultFolderPath + fileMd5 + extension;
  }
  boolean result = addMediaFilesToMinIO(localFilePath, mimetype, bucket_Files, objectName);
  if (!result){
   XueChengPlusException.cast("上传文件失败");
  }
  //文件大小
  uploadFileParamsDto.setFileSize(file.length());
  //将文件信息保存到数据库
  MediaFiles mediaFiles=currentProxy.addMediaFilesToDb(companyId,fileMd5,uploadFileParamsDto,bucket_Files,objectName);
  //准备返回数据
  UploadFileResultDto uploadFileResultDto=new UploadFileResultDto();
  BeanUtils.copyProperties(mediaFiles,uploadFileResultDto);
  return uploadFileResultDto;
 }

 /**
  * 清除分块文件
  * @param chunkFileFolderPath 分块文件路径
  * @param chunkTotal 分块文件总数
  */
 private void clearChunkFiles(String chunkFileFolderPath,int chunkTotal) {
  Iterable<DeleteObject>objects=Stream.iterate(0,i->++i).limit(chunkTotal)
          .map(i->new DeleteObject(chunkFileFolderPath+i)).collect(Collectors.toList());
  RemoveObjectsArgs removeObjectsArgs = RemoveObjectsArgs.builder().bucket(bucket_video).objects(objects).build();
  minioClient.removeObjects(removeObjectsArgs);
 }

  /**
   * 从minio下载文件
   * @param bucket 桶
   * @param objectName 对象名称
   * @return 下载后的文件
   */
 public File downloadFileFromMinIO(String bucket,String objectName){
  //临时文件
  File minioFile = null;
  FileOutputStream outputStream = null;
  try{
   InputStream stream = minioClient.getObject(GetObjectArgs.builder()
           .bucket(bucket)
           .object(objectName)
           .build());
   //创建临时文件
   minioFile=File.createTempFile("minio", ".merge");
   outputStream = new FileOutputStream(minioFile);
   IOUtils.copy(stream,outputStream);
   return minioFile;
  } catch (Exception e) {
   e.printStackTrace();
  }finally {
   if(outputStream!=null){
    try {
     outputStream.close();
    } catch (IOException e) {
     e.printStackTrace();
    }
   }
  }
  return null;
 }

 /**
  * 添加待处理任务
  * @param mediaFiles
  */
 private void addWaitingTask(MediaFiles mediaFiles){
  //文件名称
  String filename=mediaFiles.getFilename();
  //文件扩展名
  String extension=filename.substring(filename.lastIndexOf("."));
  //文件mimetype
  String mimeType=getMimeType(extension);
  //如果是avi视频添加到视频待处理表
  if (mimeType.equals("video/x-msvideo")){
   MediaProcess mediaProcess=new MediaProcess();
   BeanUtils.copyProperties(mediaFiles,mediaProcess);
   mediaProcess.setStatus("1"); //未处理
   mediaProcess.setFailCount(0);//失败次数默认为0
   mediaProcessMapper.insert(mediaProcess);
  }
 }

 //得到合并后的文件的地址
 private String getFilePathByMd5(String fileMd5,String fileExt){
  return   fileMd5.substring(0,1) + "/" + fileMd5.substring(1,2) + "/" + fileMd5 + "/" +fileMd5 +fileExt;
 }

 //得到分块文件的目录
 private String getChunkFileFolderPath(String fileMd5){
  return fileMd5.substring(0,1)+"/"+fileMd5.substring(1,2)+"/"+fileMd5+"/"+"chunk"+"/";
 }
}
