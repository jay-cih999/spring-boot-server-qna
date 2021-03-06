package com.jay.qna.controller;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jay.qna.advice.CustomException;
import com.jay.qna.dao.AnswerDao;
import com.jay.qna.dao.QnADao;
import com.jay.qna.service.QnAService;

@RestController
public class QnAController {
	
	@Autowired
	QnAService qnaService;
	
	
	@RequestMapping(method = RequestMethod.POST, value = "/qna", consumes = "application/json")
	public void addQnA(@RequestBody @Valid QnADao userInput) {
		qnaService.addQnA(userInput);
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/qna")
	public List<QnADao> findAll(@RequestParam(value = "status", required=false) String status,
			@RequestParam(value = "customerId", required=false) String customerId,
			@RequestParam(value = "name", required=false) String name,
			@RequestParam(value = "title", required=false) String title) {
		QnADao dao = new QnADao();
		
		if(status != null && status != "") {
			String[] arr = status.split(",");
			List<String> statusList = new ArrayList<String>();
			for(int i = 0; i < arr.length; i++) {
				statusList.add(arr[i]);
			}
			dao.setStatusList(statusList);
		} 
		if(customerId != null && customerId != "") {
			dao.setCustomer_id(customerId);
		} if(name != null && name != "") {
			dao.setName(name);
		} if(title != null && title != "") {
			dao.setTitle(title);
		}
		
		return qnaService.findAll(dao);
	}
	
	
	@RequestMapping(method = RequestMethod.GET, value = "/qna/{id}")
	public QnADao findOneQna(@PathVariable("id") Long id) {
		QnADao dao = new QnADao();
		
		if(id != null && id != 0) {
			dao.setId(id);
		} else {
			throw new CustomException("id ?????? ??????????????? ????????????.(0??????)");
		}
		
		QnADao result = qnaService.findOne(dao);
		
		if(result != null) {
			return result;
		} else {
			throw new CustomException("?????? id ????????? ?????? ?????? ????????????.");
		}
			
	}
	
	@RequestMapping(method = RequestMethod.POST, value = "/qna/assign/{id}", consumes = "application/json" )
	public ResponseEntity<QnADao> assignQna(@RequestBody QnADao userInput, @PathVariable("id") Long id) {
		if(id == null || id == 0) {
			throw new CustomException("Q&A ID??? ?????? ????????? ????????????.");
		}
		
		userInput.setId(id);
		QnADao result = qnaService.findOne(userInput);
		
		if(result == null) {
			throw new CustomException("???????????? ?????? ?????? ?????????.");
		}
		
		if(!"submit".equals(result.getStatus())) {
			throw new CustomException("?????? ?????? ???????????????.");
		}
		
		qnaService.assignQna(userInput);
		QnADao dao = qnaService.findOne(userInput);
		
		return new ResponseEntity<>(dao, HttpStatus.OK);
	}
	
	@RequestMapping(method = RequestMethod.POST, value = "/qna/answer/{id}")
	public ResponseEntity<QnADao> qnaAnswer(@RequestBody AnswerDao userInput, @PathVariable("id") Long id) {
		if(id == null || id == 0) {
			throw new CustomException("Q&A ID??? ?????? ????????? ????????????.");
		}
		
		userInput.setQna_id(id);
		
		QnADao query = new QnADao();
		query.setId(id);
		
		QnADao result = qnaService.findOne(query);
		
		if(result == null) {
			throw new CustomException("???????????? ?????? ?????? ?????????.");
		}
		
		if("submit".equals(result.getStatus())) {
			throw new CustomException("????????? ?????? ?????? ????????? ????????????.");
		} else if("complete".equals(result.getStatus())) {
			throw new CustomException("?????? ????????? ?????? ?????????.");
		}
		
		if(!result.getManagerId().equals((userInput.getUser().getId()))) {
			throw new CustomException("?????? ?????? ??????????????? ????????? ?????? ?????????.");
		}
		
		if(userInput.getReply() == null || "".equals(userInput.getReply())) {
			throw new CustomException("????????? ?????? ????????????.");
		}
		qnaService.inserAnswer(userInput);
		qnaService.updateStatusComplete(query);
		
		QnADao dao = qnaService.findOne(query);
		
		return new ResponseEntity<>(dao, HttpStatus.OK);
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/qna/{id}/pass")
	public QnADao findOneQnaWithPassword(@PathVariable("id") Long id,
			@RequestParam String password) {
		QnADao dao = new QnADao();
		
		if(id != null && id != 0) {
			dao.setId(id);
		} else {
			throw new CustomException("id ?????? ??????????????? ????????????.(0??????)");
		}
		
		if(password != null && !"".equals(password)) {
			dao.setPassword(password);
		} else {
			throw new CustomException("Password ?????? ??????????????? ????????????.");
		}
		
		QnADao result = qnaService.findOneWithPassword(dao);
		
		if(result != null) {
			return result;
		} else {
			throw new CustomException("?????? ????????? ??? ??? ???????????????.");
		}
	}
}
