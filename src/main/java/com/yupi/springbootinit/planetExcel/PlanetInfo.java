package com.yupi.springbootinit.planetExcel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 星球用户表格信息
 */
@Data
public class PlanetInfo {

    /**
     * 用户名
     */
    @ExcelProperty("成员昵称")
    private String username;
}
