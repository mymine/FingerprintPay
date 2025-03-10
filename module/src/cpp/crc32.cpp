#include <stdio.h>
#include <stdint.h>
#include <stdlib.h>
#include "crc32.h"
#include "log.h"

// 静态成员初始化
uint32_t CRC32::crc32_table[256];
bool CRC32::table_initialized = false;

// 初始化CRC32表
void CRC32::initCRC32Table() {
    uint32_t c;
    for (int i = 0; i < 256; i++) {
        c = i;
        for (int j = 0; j < 8; j++) {
            c = (c & 1) ? (0xEDB88320 ^ (c >> 1)) : (c >> 1);
        }
        crc32_table[i] = c;
    }
    table_initialized = true;
}

// 获取文件大小，文件不存在返回-1
size_t CRC32::getFileSize(const char* filename) {
    FILE* file = fopen(filename, "rb");
    if (!file) {
        LOGE("Failed to open file: %s", filename);
        return -1;  // 文件不存在
    }
    
    fseek(file, 0, SEEK_END);
    size_t size = ftell(file);
    fclose(file);
    
    return size;
}

// 计算文件的CRC32值
uint32_t CRC32::calculateFileCRC32(const char* filename) {
    if (!table_initialized) {
        initCRC32Table();
    }
    
    FILE* file = fopen(filename, "rb");
    if (!file) {
        LOGE("Failed to open file: %s", filename);
        return 0;
    }
    
    const size_t buffer_size = 8192; // 8KB缓冲区
    unsigned char* buffer = (unsigned char*)malloc(buffer_size);
    if (!buffer) {
        LOGE("Failed to allocate memory for buffer");
        fclose(file);
        return 0;
    }
    
    uint32_t crc = 0xFFFFFFFF;
    size_t bytes_read;
    
    // 一次读取多个字节以提高性能
    while ((bytes_read = fread(buffer, 1, buffer_size, file)) > 0) {
        for (size_t i = 0; i < bytes_read; i++) {
            crc = crc32_table[(crc ^ buffer[i]) & 0xFF] ^ (crc >> 8);
        }
    }
    
    free(buffer);
    fclose(file);
    return crc ^ 0xFFFFFFFF;
}

// 验证文件CRC32是否匹配预期值
bool CRC32::verifyFileCRC32(const char* filename, uint32_t expected_crc) {
    uint32_t actual_crc = calculateFileCRC32(filename);
    return (actual_crc == expected_crc);
}

// 验证文件大小和CRC32是否匹配预期值
bool CRC32::verifyFileSizeAndCRC32(const char* filename, size_t expected_size, uint32_t expected_crc) {
    // 首先检查文件大小
    size_t actual_size = getFileSize(filename);
    if (actual_size == -1) {
        LOGE("File does not exist: %s", filename);
        return false;
    }
    
    if (actual_size != expected_size) {
        LOGE("File size mismatch. Expected: %zu, Actual: %zu", expected_size, actual_size);
        return false;
    }
    
    // 如果大小匹配，再检查CRC32
    uint32_t actual_crc = calculateFileCRC32(filename);
    if (actual_crc != expected_crc) {
        LOGE("CRC32 mismatch. Expected: 0x%08X, Actual: 0x%08X", expected_crc, actual_crc);
        return false;
    }
    
    LOGI("File verification passed: %s", filename);
    return true;
}