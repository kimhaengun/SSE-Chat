package com.cos.reactorex2;

import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import reactor.core.publisher.Sinks.EmitResult;

@CrossOrigin
@RestController
public class Testcontroller2 { //프로세서를 사용하고싶다 = 중간에 데이터가 들어와도 계속 실행된다.
	//프로세서 = 지속적 응답 -->response를 끊지않는다.
	
	Sinks.Many<String> sink;
	
	//multicast() = 새로 들어온 데이터만 응답받음 hot 시퀀스(스트림)
	//reply() = 기존데이터 + 새로운 데이터 응답 cold 시퀀스
	public Testcontroller2() {
		// TODO Auto-generated constructor stub
		this.sink = Sinks.many().multicast().onBackpressureBuffer();
	}
	
	@GetMapping(value = "/",produces = MediaType.APPLICATION_JSON_VALUE)
	public Flux<Integer> findAll(){
		return Flux.just(1,2,3,4,5,6).log();
	}
	
	
	@GetMapping("/send")
	public void send(String username, String msg) {
		String content = username + " : " + msg;
		sink.tryEmitNext(content);
	}
	
	// data = 실제값 \n\n
	@GetMapping(value = "/sse",produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public Flux<ServerSentEvent<String>> sse() { //ServerSendEvent의 ContentType은 text event stream
		return sink.asFlux().map(e->ServerSentEvent.builder(e).build()).doOnCancel(()->{
			System.out.println("SSE 종료됨");
			sink.asFlux().blockLast();
		}); //구독
	}

}
