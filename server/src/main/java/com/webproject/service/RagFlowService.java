//package com.webproject.service;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.webproject.config.RagFlowProperties;
//import com.webproject.pojo.*;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.HttpMethod;
//import org.springframework.http.MediaType;
//import org.springframework.stereotype.Service;
//import org.springframework.web.reactive.function.client.WebClient;
//import reactor.core.publisher.Flux;
//import reactor.core.publisher.Mono;
//
//import java.time.Duration;
//
///**
// * RAGFlow服务类
// */
//@Slf4j
//@Service
//public class RagFlowService {
//
//    private final RagFlowProperties properties;
//    private final WebClient webClient;
//    private final ObjectMapper objectMapper;
//
//    public RagFlowService(RagFlowProperties properties,ObjectMapper objectMapper)
//    {
//        this.properties = properties;
//        this.objectMapper = objectMapper;
//        this.webClient = WebClient.builder()
//                .baseUrl(properties.getBaseUrl())
//                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
//                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + properties.getApiKey())
//                .build();
//    }
//
//
//    /**
//     * 与RAGFlow聊天助手对话（流式）
//     *
//     * @param chatId 聊天助手ID
//     * @param request 聊天请求
//     * @return 流式响应（SSE格式）
//     */
//    public Flux<String> chatCompletionStream(String chatId, ChatRequest request) {
//        String url = "/api/v1/chats/" + chatId + "/completions";
//
//        // 使用 setter 方式构建请求对象
//        RagFlowchatRequest requestBody = new RagFlowchatRequest();
//        requestBody.setQuestion(request.getQuestion());
//        requestBody.setStream(true);
//        requestBody.setSessionId(request.getSessionId());
//        requestBody.setUserId(request.getUserId());
//
//        log.info("调用RAGFlow API (流式): {}, 请求: {}", url, requestBody);
//
//        return webClient.post()
//                .uri(url)
//                .bodyValue(requestBody)  // WebClient 会自动将 POJO 序列化为 JSON
//                .accept(MediaType.TEXT_EVENT_STREAM)
//                .retrieve()
//                .bodyToFlux(String.class)
//                .timeout(Duration.ofMinutes(5))
//                .filter(line -> line != null && !line.trim().isEmpty())
//                .doOnError(error -> log.error("调用RAGFlow API (流式)失败", error));
//    }
//
//    /**
//     * 创建会话
//     *
//     * @param chatId 聊天助手ID
//     * @param sessionName 会话名称
//     * @param userId 用户自定义ID（可选）
//     * @return 统一返回结果
//     */
//    public Mono<Result> createSession(String chatId, String sessionName, String userId) {
//        String url = "/api/v1/chats/" + chatId + "/sessions";
//
//        // 使用 setter 方式构建请求对象
//        SessionCreateRequest requestBody = new SessionCreateRequest();
//        requestBody.setName(sessionName);
//        requestBody.setUserId(userId);
//
//        log.info("创建会话: {}, 请求: {}", url, requestBody);
//
//        return webClient.post()
//                .uri(url)
//                .bodyValue(requestBody)  // WebClient 会自动将 POJO 序列化为 JSON
//                .retrieve()
//                .bodyToMono(String.class)
//                .timeout(Duration.ofSeconds(30))
//                .map(responseBody -> {
//                    try {
//                        SessionResponse response = objectMapper.readValue(responseBody, SessionResponse.class);
//                        if (response.getCode() == 0) {
//                            return Result.success(response.getData());
//                        } else {
//                            return Result.error(response.getMessage() != null ? response.getMessage() : "创建会话失败");
//                        }
//                    } catch (Exception e) {
//                        log.error("解析会话响应失败: {}", responseBody, e);
//                        return Result.error("解析响应失败: " + e.getMessage());
//                    }
//                });
//    }
//
//    /**
//     * 获取会话列表
//     *
//     * @param chatId 聊天助手ID
//     * @param page 页码（可选，默认1）
//     * @param pageSize 每页数量（可选，默认30）
//     * @return 统一返回结果
//     */
//    public Mono<Result> listSessions(String chatId, Integer page, Integer pageSize) {
//        StringBuilder url = new StringBuilder("/api/v1/chats/" + chatId + "/sessions");
//
//        log.info("获取会话列表: {}", url);
//        return webClient.get()
//                .uri(url.toString())
//                .retrieve()
//                .bodyToMono(String.class)
//                .timeout(Duration.ofSeconds(30))
//                .map(responseBody -> {
//                    try {
//                        SessionResponse response = objectMapper.readValue(responseBody, SessionResponse.class);
//                        if (response.getCode() == 0) {
//                            return Result.success(response.getData());
//                        } else {
//                            return Result.error(response.getMessage() != null ? response.getMessage() : "获取会话列表失败");
//                        }
//                    } catch (Exception e) {
//                        log.error("解析会话列表响应失败: {}", responseBody, e);
//                        return Result.error("解析响应失败: " + e.getMessage());
//                    }
//                })
//                .onErrorResume(error -> {
//                    log.error("获取会话列表失败", error);
//                    return Mono.just(Result.error("获取会话列表失败: " + error.getMessage()));
//                });
//    }
//
//    /**
//     * 删除会话
//     *
//     * @param chatId 会话所属聊天助手ID
//     * @param deleteRequest 删除请求（包含ids，可选）
//     * @return 统一返回结果
//     */
//    public Mono<Result> deleteSessions(String chatId, SessionDeleteRequest deleteRequest) {
//        String url = "/api/v1/chats/" + chatId + "/sessions";
//
//        log.info("删除会话: {}, 请求: {}", url, deleteRequest);
//
//        return webClient.method(HttpMethod.DELETE)
//                .uri(url)
//                .bodyValue(deleteRequest != null ? deleteRequest : new SessionDeleteRequest())
//                .retrieve()
//                .bodyToMono(String.class)
//                .timeout(Duration.ofSeconds(30))
//                .map(responseBody -> {
//                    try {
//                        SessionResponse response = objectMapper.readValue(responseBody, SessionResponse.class);
//                        if (response.getCode() == 0) {
//                            return Result.success(response.getData());
//                        } else {
//                            return Result.error(response.getMessage() != null ? response.getMessage() : "删除会话失败");
//                        }
//                    } catch (Exception e) {
//                        log.error("解析删除会话响应失败: {}", responseBody, e);
//                        return Result.error("解析响应失败: " + e.getMessage());
//                    }
//                })
//                .onErrorResume(error -> {
//                    log.error("删除会话失败", error);
//                    return Mono.just(Result.error("删除会话失败: " + error.getMessage()));
//                });
//    }
//}
//
