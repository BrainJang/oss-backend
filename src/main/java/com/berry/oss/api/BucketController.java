package com.berry.oss.api;

import com.berry.oss.common.Result;
import com.berry.oss.common.ResultFactory;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

/**
 * Title BucketController
 * Description
 * Copyright (c) 2019
 * Company  上海思贤信息技术股份有限公司
 *
 * @author berry_cooper
 * @version 1.0
 * @date 2019/6/4 14:56
 */
@RestController
@RequestMapping("api/bucket")
public class BucketController {

    @GetMapping("list")
    @ApiOperation("获取 Bucket 列表")
    public Result list(){
        return ResultFactory.wrapper();
    }

    @PostMapping("create")
    @ApiOperation("创建 Bucket")
    public Result create(){
        return ResultFactory.wrapper();
    }

    @GetMapping("detail")
    @ApiOperation("获取 Bucket 详情")
    public Result detail(){
        return ResultFactory.wrapper();
    }

    @DeleteMapping("delete")
    @ApiOperation("删除 Bucket")
    public Result delete(){
        return ResultFactory.wrapper();
    }

    @PutMapping("updateBucketAcl")
    @ApiOperation("更新 Bucket 读写权限")
    public Result updateBucketAcl(){
        return ResultFactory.wrapper();
    }
}
