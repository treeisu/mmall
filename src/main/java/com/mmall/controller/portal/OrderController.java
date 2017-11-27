package com.mmall.controller.portal;

import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.OrderItem;
import com.mmall.pojo.User;
import com.mmall.service.IOrderService;
import com.mmall.vo.OrderVo;


/**
 * 
 * @ClassName: OrderController
 * @Description: TODO 订单处理类【包括支付处理】
 * @author Barry
 * @date 2017年11月23日 下午5:13:22
 *
 */
@Controller
@RequestMapping("/order")
public class OrderController {
	private Logger logger=LoggerFactory.getLogger(OrderController.class);
	@Autowired
	private IOrderService iOrderService;
	
	
	
	@RequestMapping(value="/create",method=RequestMethod.GET)
	@ResponseBody
	public ServerResponse<OrderVo> creat(HttpSession session,Integer shippingId){
		User user=(User) session.getAttribute(Const.CURRENT_USER);
		if(user==null){
			return ServerResponse.createByErrorMessage(ResponseCode.NEED_LOGIN.getCode(),"当前处于未登录状态，请登录系统！");
		}
		ServerResponse<OrderVo> response=iOrderService.createOrder(user.getId(), shippingId);		
		return response;		
	}
	/**
	 * 
	 * @Title: pay
	 * @Description: TODO 支付接口【支付宝返回一个二维码】
	 * @param @param session
	 * @param @param orderNo
	 * @param @param request
	 * @param @return    
	 * @return ServerResponse<Map<String,String>>    
	 * @throws
	 */
	@RequestMapping(value="/pay",method=RequestMethod.GET)
	@ResponseBody
	public ServerResponse<Map<String, String>> pay(HttpSession session,Long orderNo,HttpServletRequest request){
		User user=(User) session.getAttribute(Const.CURRENT_USER);
		if(user==null){
			return ServerResponse.createByErrorMessage(ResponseCode.NEED_LOGIN.getCode(),"当前处于未登录状态，请登录系统！");
		}
		//获得项目路径下的upload文件夹【用于暂存二维码】
		String path=request.getSession().getServletContext().getRealPath("upload");
		//调用支付宝【当面付】接口，得到结果，如果成功则返回二维码的存放路径
		ServerResponse<Map<String, String>> response=iOrderService.pay(orderNo, user.getId(), path);
		return response;		
	}
	/**
	 * 
	 * @Title: AliPay_CallBack
	 * @Description: TODO 用户扫描二维码，点击支付后，支付宝回调此接口，我们需要返回核对结果【失败或者成功】
	 * @param @param request
	 * @param @return    
	 * @return Object    
	 * @throws
	 */
	@RequestMapping(value="/alipay_callback",method=RequestMethod.POST)
	@ResponseBody
	public String AliPay_CallBack(HttpServletRequest request){
		//获得所有参数
		Map<String,String[]> requestParams=request.getParameterMap();			
		//将参数对对应的值  数组类型转换成字符串【用逗号拼接】		
		logger.info("支付宝回调,sign:{},trade_status:{},参数:{}",requestParams.get("sign"),requestParams.get("trade_status"),requestParams.toString());
		ServerResponse<String> response=iOrderService.aliCallBackCheck(requestParams);		
		if(response.isSuccess()){
			return Const.AlipayCallback.RESPONSE_SUCCESS;
		}else{
			return Const.AlipayCallback.RESPONSE_FAILED;	
		}			
	}
	/**
	 * 
	 * @Title: queryOrderPayStatus
	 * @Description: TODO 查询某个订单的支付状态是否是支付成功的
	 * @param @param session
	 * @param @param orderNo
	 * @param @return    
	 * @return ServerResponse<Boolean>    
	 * @throws
	 */
	@RequestMapping(value="/query_order_pay_status",method=RequestMethod.GET)
	@ResponseBody
	public ServerResponse<Boolean> queryOrderPayStatus(HttpSession session,Long orderNo){
		User user=(User) session.getAttribute(Const.CURRENT_USER);
		if(user==null){
			return ServerResponse.createByErrorMessage(ResponseCode.NEED_LOGIN.getCode(),"当前处于未登录状态，请登录系统！");
		}		
		ServerResponse<Boolean> response=iOrderService.queryOrderPayStatusIsSuccess(orderNo, user.getId());
		if(response.isSuccess()){
			return ServerResponse.createBySuccessMessage(true);
		}else{
			return ServerResponse.createBySuccessMessage(false);
		}				
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
