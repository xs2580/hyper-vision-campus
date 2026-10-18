//package com.webproject.controller;
//
//import com.webproject.pojo.ChatRequest;
//import com.webproject.pojo.Result;
//import com.webproject.pojo.SessionDeleteRequest;
//import com.webproject.service.RagFlowService;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.MediaType;
//import org.springframework.web.bind.annotation.*;
//import reactor.core.publisher.Flux;
//import reactor.core.publisher.Mono;
//
///**
// * 聊天控制器
// */
//@Slf4j
//@RestController
//@RequestMapping("/api/chat")
//public class Chatcontorller {
//
//    @Autowired
//    private RagFlowService ragFlowService;
//
//    /**
//     * 聊天接口（流式）
//     * @param chatId 聊天助手ID
//     * @param request 聊天请求（包含question和sessionId）
//     * @return 流式响应
//     */
//    @PostMapping(value = "/{chatId}/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
//    public Flux<String> chatStream(@PathVariable String chatId, @RequestBody ChatRequest request) {
//        if (request.getQuestion() == null || request.getQuestion().trim().isEmpty()) {
//            return Flux.error(new RuntimeException("问题不能为空"));
//        }
//        return ragFlowService.chatCompletionStream(chatId, request)
//                .onErrorResume(error -> {
//                    log.error("流式聊天失败", error);
//                    return Flux.just("data: " + error.getMessage());
//                });
//    }
//
//    /**
//     * 创建会话
//     * @param chatId 聊天助手ID
//     * @param sessionName 会话名称
//     * @param userId 用户自定义ID（可选）
//     * @return 会话信息（包含会话ID和历史消息）
//     */
//    @PostMapping("/{chatId}/sessions")
//    public Mono<Result> createSession(@PathVariable String chatId,
//                                      @RequestParam String sessionName,
//                                      @RequestParam(required = false) String userId) {
//        return ragFlowService.createSession(chatId, sessionName, userId);
//    }
//
//    /**
//     * 获取会话列表（包含会话历史）
//     * @param chatId 聊天助手ID
//     * @param page 页码（可选，默认1）
//     * @param pageSize 每页数量（可选，默认30）
//     * @return 会话列表（每个会话包含历史消息）
//     */
//    @GetMapping("/{chatId}/sessions")
//    public Mono<Result> listSessions(@PathVariable String chatId,
//                                     @RequestParam(required = false, defaultValue = "1") Integer page,
//                                     @RequestParam(required = false, defaultValue = "30") Integer pageSize) {
//        return ragFlowService.listSessions(chatId, page, pageSize);
//    }
//
//    /**
//     * 删除会话
//     * @param chatId 聊天助手ID
//     * @param request 请求体，可选，包含需要删除的会话ID集合
//     * @return 删除结果
//     */
//    @DeleteMapping("/{chatId}/sessions")
//    public Mono<Result> deleteSession(@PathVariable String chatId,
//                                      @RequestBody(required = false) SessionDeleteRequest request) {
//        return ragFlowService.deleteSessions(chatId, request);
//    }
//}
