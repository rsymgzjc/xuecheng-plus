package com.xuecheng.media.service;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.base.model.RestResponse;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFileParamsDto;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import com.xuecheng.media.model.po.MediaFiles;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.File;
import java.util.List;

/**
 * @description 媒资文件管理业务类
 * @author Mr.M
 * @date 2022/9/10 8:55
 * @version 1.0
 */
public interface MediaFileService {

 /**
  * @description 媒资文件查询方法
  * @param pageParams 分页参数
  * @param queryMediaParamsDto 查询条件
  * @return com.xuecheng.base.model.PageResult<com.xuecheng.media.model.po.MediaFiles>
  * @author Mr.M
  * @date 2022/9/10 8:57
 */
 public PageResult<MediaFiles> queryMediaFiels(Long companyId,PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto);

 /**
  * 上传文件
  * @param companyId
  * @param uploadFileParamsDto
  * @param localFilePath
  * @return
  */
 public UploadFileResultDto uploadFile(Long companyId,UploadFileParamsDto uploadFileParamsDto,String localFilePath,String objectName);

 /**
  * 将文件信息添加到文件表
  * @param companyId
  * @param fileMd5
  * @param uploadFileParamsDto
  * @param bucket
  * @param objectName
  * @return
  */
 public MediaFiles addMediaFilesToDb(Long companyId,String fileMd5,UploadFileParamsDto uploadFileParamsDto,String bucket,String objectName);
 /**
  * 检查文件是否存在
  * @param fileMd5
  * @return
  */
 public RestResponse<Boolean> checkFile(String fileMd5);
 /**
  * 检查分块是否存在
  * @param fileMd5
  * @param chunkIndex
  * @return
  */
 public RestResponse<Boolean> checkChunk(String fileMd5, int chunkIndex);

 /**
  * 上传分块
  * @param fileMd5
  * @param chunk
  * @param localChunkFilePath
  * @return
  */
 public RestResponse uploadChunk(String fileMd5,int chunk,String localChunkFilePath);

 /**
  * 合并分块
  * @param companyId
  * @param fileMd5
  * @param chunkTotal
  * @param uploadFileParamsDto
  * @return
  */
 public RestResponse mergechunks(Long companyId,String fileMd5,int chunkTotal,UploadFileParamsDto uploadFileParamsDto);
 /**
  * 从minio下载文件
  * @param bucket 桶
  * @param objectName 对象名称
  * @return 下载后的文件
  */
 public File downloadFileFromMinIO(String bucket, String objectName);
 /**
  * 将文件写入minio
  * @param localFilePath
  * @param mimeType
  * @param bucket
  * @param objectName
  * @return
  */
 public boolean addMediaFilesToMinIO(String localFilePath,String mimeType,String bucket, String objectName);

 /**
  * 根据媒资ID查询文件信息
  * @param mediaId
  * @return
  */
 MediaFiles getFileById(String mediaId);
}
