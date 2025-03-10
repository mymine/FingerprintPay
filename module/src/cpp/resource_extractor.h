#ifndef RESOURCE_EXTRACTOR_H
#define RESOURCE_EXTRACTOR_H

#ifdef __cplusplus
extern "C" {
#endif

/**
 * 将嵌入的DEX资源提取到指定路径
 * 
 * @param outputPath DEX文件的输出路径
 * @return 是否成功提取，成功返回true，失败返回false
 */
bool extractDexResource(const char* outputPath);

#ifdef __cplusplus
}
#endif

#endif // RESOURCE_EXTRACTOR_H