package com.yupi.springbootinit.planetExcel;

import com.alibaba.excel.EasyExcel;

import java.util.List;

public class importExcel {
    public static void main(String[] args) {
        String fileName = "D:\\JavaProject\\FindFriendsBackend\\TestData\\testFind.xlsx";
        // 这里 需要指定读用哪个class去读，然后读取第一个sheet 同步读取会自动finish
        List<PlanetInfo> list = EasyExcel.read(fileName).head(PlanetInfo.class).sheet().doReadSync();
        for (PlanetInfo data : list) {
            System.out.println(data);
        }

    }
}
