package com.panjx.clouddrive.core.ui

import com.panjx.clouddrive.core.modle.File

object FilePreviewParameterData {

    val FILE= File(
        id = "1",
        name = "sunset.jpg",
        size = 3_145_728,
        type = "image/jpeg",
        parentId = "0",
        path = "/photos",
        createTime = 1625068800000,  // 2021-07-01
        updateTime = 1625155200000,
        isDir = false
    )

    val FILES = listOf(
        // 图像类文件
        FILE,
        File(
            id = "2",
            name = "diagram",
            size = 0,
            type = "Folder",
            parentId = "0",
            path = "/design",
            createTime = 1625241600000,
            updateTime = 1625328000000,
            isDir = false
        ),

        // 文档类文件
        File(
            id = "3",
            name = "contract.docx",
            size = 524_288,
            type = "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            parentId = "0",
            path = "/documents",
            createTime = 1625414400000,
            updateTime = 1625500800000,
            isDir = false
        ),
        File(
            id = "4",
            name = "manual.pdf",
            size = 2_097_152,
            type = "application/pdf",
            parentId = "0",
            path = "/documents",
            createTime = 1625587200000,
            updateTime = 1625673600000,
            isDir = false
        ),

        // 媒体文件
        File(
            id = "5",
            name = "interview.mp3",
            size = 8_388_608,
            type = "audio/mpeg",
            parentId = "0",
            path = "/media",
            createTime = 1625760000000,
            updateTime = 1625846400000,
            isDir = false
        ),
        File(
            id = "6",
            name = "tutorial.mp4",
            size = 52_428_800,
            type = "video/mp4",
            parentId = "0",
            path = "/videos",
            createTime = 1625932800000,
            updateTime = 1626019200000,
            isDir = false
        ),

        // 开发相关
        File(
            id = "7",
            name = "app-release.apk",
            size = 15_728_640,
            type = "application/vnd.android.package-archive",
            parentId = "0",
            path = "/downloads",
            createTime = 1626105600000,
            updateTime = 1626192000000,
            isDir = false
        ),
        File(
            id = "8",
            name = "config.json",
            size = 2_048,
            type = "application/json",
            parentId = "0",
            path = "/system",
            createTime = 1626278400000,
            updateTime = 1626364800000,
            isDir = false
        ),

        // 压缩包
        File(
            id = "9",
            name = "backup.zip",
            size = 10_485_760,
            type = "application/zip",
            parentId = "0",
            path = "/archives",
            createTime = 1626451200000,
            updateTime = 1626537600000,
            isDir = false
        ),
        File(
            id = "10",
            name = "dataset.rar",
            size = 31_457_280,
            type = "application/x-rar-compressed",
            parentId = "0",
            path = "/archives",
            createTime = 1626624000000,
            updateTime = 1626710400000,
            isDir = false
        ),

        // 代码文件
        File(
            id = "11",
            name = "main.kt",
            size = 4_096,
            type = "text/x-kotlin",
            parentId = "0",
            path = "/projects",
            createTime = 1626796800000,
            updateTime = 1626883200000,
            isDir = false
        ),
        File(
            id = "12",
            name = "build.gradle",
            size = 1_024,
            type = "text/plain",
            parentId = "0",
            path = "/projects",
            createTime = 1626969600000,
            updateTime = 1627056000000,
            isDir = false
        ),

        // 特殊类型
        File(
            id = "13",
            name = "database.sqlite",
            size = 104_857_600,
            type = "application/x-sqlite3",
            parentId = "0",
            path = "/data",
            createTime = 1627142400000,
            updateTime = 1627228800000,
            isDir = false
        ),
        File(
            id = "14",
            name = "script.sh",
            size = 512,
            type = "application/x-sh",
            parentId = "0",
            path = "/scripts",
            createTime = 1627315200000,
            updateTime = 1627401600000,
            isDir = false
        ),

        // 子目录示例
        File(
            id = "15",
            name = "Projects",
            size = 0,
            type = "Folder",
            parentId = "0",
            path = "/projects",
            createTime = 1630454400000,
            updateTime = 1630540800000,
            isDir = true
        ),
        // 子目录中的文件
        File(
            id = "16",
            name = "design.sketch",
            size = 5_242_880,
            type = "application/sketch",
            parentId = "3",
            path = "/projects/design",
            createTime = 1630627200000,
            updateTime = 1630713600000,
            isDir = false
        ),
        File(
            id = "17",
            name = "meeting_recording.mp4",
            size = 24_576_000,
            type = "video/mp4",
            parentId = "3",
            path = "/projects/recordings",
            createTime = 1630800000000,
            updateTime = 1630886400000,
            isDir = false
        ),
        // 嵌套子目录
        File(
            id = "18",
            name = "Backups",
            size = 0,
            type = "directory",
            parentId = "15",
            path = "/projects/backups",
            createTime = 1631318400000,
            updateTime = 1631404800000,
            isDir = true
        ),
        File(
            id = "19",
            name = "backup1",
            size = 0,
            type = "directory",
            parentId = "18",
            path = "/projects/backups/",
            createTime = 1631392000000,
            updateTime = 1631478400000,
            isDir = true
        ),
        File(
            id = "20",
            name = "backup2",
            size = 0,
            type = "directory",
            parentId = "19",
            path = "/projects/backups1/",
            createTime = 1631478400000,
            updateTime = 1631564800000,
            isDir = true
        ),
    )
}