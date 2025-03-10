#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/stat.h>
#include <unistd.h>
#include "log.h"
#include "crc32.h"

// 声明外部符号，这些符号是由objcopy工具生成的
extern "C" {
    // DEX资源的开始、结束地址和大小
    extern const uint8_t _binary_classes_dex_start[];
    extern const uint8_t _binary_classes_dex_end[];
    extern const size_t _binary_classes_dex_size; // BUG, size zero
}

/**
 * 将嵌入的DEX资源提取到指定路径
 * 
 * @param outputPath DEX文件的输出路径
 * @return 是否成功提取
 */
extern "C" bool extractDexResource(const char* outputPath) {
    // 计算资源大小
    size_t resourceSize = reinterpret_cast<size_t>(_binary_classes_dex_end) - 
                         reinterpret_cast<size_t>(_binary_classes_dex_start);
    // 验证文件大小和CRC32
    if (CRC32::verifyFileSizeAndCRC32(outputPath, resourceSize, RESOURCE_CRC32)) {
        LOGI("%s verification passed!", outputPath);
        return true;
    } else {
        LOGE("%s verification failed!", outputPath);
    }

    LOGI("Extracting DEX resource to: %s", outputPath);
    if (access(outputPath, 0) == 0 && remove(outputPath) != 0) {
        LOGE("Error delete file %s reason: %s", outputPath, strerror(errno));
        return -1;
    }
    FILE* outFile = fopen(outputPath, "wb");
    if (!outFile) {
        LOGE("Failed to create output file: %s", outputPath);
        return false;
    }
    
    // 写入DEX数据
    size_t written = fwrite(_binary_classes_dex_start, 1, resourceSize, outFile);
    
    // 检查是否写入成功
    if (written != resourceSize) {
        LOGE("Failed to write DEX data. Written: %zu, Expected: %zu", written, resourceSize);
        fclose(outFile);
        return false;
    }
    
    // 关闭文件
    fclose(outFile);
    
    // 设置文件权限
    chmod(outputPath, S_IRUSR | S_IRGRP | S_IROTH);
    
    LOGI("DEX resource extraction successful, size: %zu bytes", resourceSize);
    return true;
}