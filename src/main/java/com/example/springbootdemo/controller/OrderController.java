package com.example.springbootdemo.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.springbootdemo.pojo.Goods;
import com.example.springbootdemo.pojo.Order;
import com.example.springbootdemo.pojo.OrderDetail;
import com.example.springbootdemo.pojo.User;
import com.example.springbootdemo.service.OrderServices;
import com.example.springbootdemo.service.UserServices;
import com.example.springbootdemo.tools.IdGenerator;
import com.example.springbootdemo.tools.JwtHelper;
import com.example.springbootdemo.tools.ResponseCode;
import com.example.springbootdemo.tools.Result;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping(value = "/api/order")
public class OrderController {
    @Autowired
    private OrderServices orderServices;
    @Autowired
    private JwtHelper jwtHelper;
    @Autowired
    private UserServices userServices;


    @RequestMapping(value = "getOrderList",method = RequestMethod.POST)
    public Result getOrderList(@RequestParam String token){
        String userName = jwtHelper.getTokenUserName(token).toString();
        User user = new User();
        user = userServices.selectByUserName(userName);
        List<Order> orderList = null;
        orderList = orderServices.getOrdersList(user.getId());
        if(orderList!=null){
            return Result.success(orderList,"获取历史订单成功！");
        }else{
            return Result.fail(ResponseCode.ERROR.val(),"获取历史订单失败！");
        }
    }


    @RequestMapping(value = "/addOrder")
    @ResponseBody
    public Result addOrder(@RequestBody JSONObject data){
        JSONObject json = JSON.parseObject(data.toJSONString());
        String orderList = json.getString("list");
        String token = json.getString("token");
        Integer pay_type = json.getString("pay_type")==null?null:Integer.valueOf(json.getString("pay_type"));
        String userName = jwtHelper.getTokenUserName(token).toString();
        User user = new User();
        user=userServices.selectByUserName(userName);
        System.out.println();
        Double total_price = 0d;
        Integer total_num = 0;
        JSONArray list = null;
        if(!orderList.isEmpty()){
            list = JSONArray.parseArray(orderList);
            for(int i=0;i<list.size();i++){
                int num = JSONObject.parseObject(JSONObject.toJSONString(list.get(i))).getInteger("count");
                total_price = total_price + JSONObject.parseObject(JSONObject.toJSONString(list.get(i))).getDouble("price")*num;
                total_num = total_num + JSONObject.parseObject(JSONObject.toJSONString(list.get(i))).getInteger("count");
            }
        }
        String order_id = IdGenerator.get();
        Order order = new Order(total_num,total_price,order_id,user.getId(),pay_type);
        if(pay_type!=null)
            order.setIs_pay(1);
        int ans = 0;
        try{
            ans = orderServices.addOrder(order,list);
        }catch (Exception e){
            e.printStackTrace();
        }
        if(ans!=0){
            if(ans==2){
                return Result.fail(ResponseCode.ERROR.val(),"库存不足！");
            }
            if(pay_type!=null)
                return Result.success(1,"下单成功！");
            else
                return Result.success(2,"下单成功，请立即支付！");
        }else{
            return Result.fail(ResponseCode.ERROR.val(),"下单失败!");
        }
    }

    @RequestMapping(value = "/deleteOrder",method = RequestMethod.POST)
    public Result deleteOrder(@RequestParam String order_id){
        int ans = 0;
        ans = orderServices.deleteOrder(order_id);
        if(ans!=0){
            return Result.success(null,"删除订单成功！");
        }else{
            return Result.fail(ResponseCode.ERROR.val(),"删除订单失败！");
        }
    }

    @RequestMapping(value = "/payOrder",method = RequestMethod.POST)
    public Result payOrder(@RequestParam String order_id,@RequestParam Integer pay_type){
        int ans = 0;
        Order order = null;
        try{
            order = orderServices.selectOrderById(order_id);
            order.setIs_pay(1);
            order.setPay_type(pay_type);
            ans = orderServices.updateOrder(order);
        }catch (Exception e){
            e.printStackTrace();
        }
        if(ans!=0){
            return Result.success(null,"支付成功！");
        }else{
            return Result.fail(ResponseCode.ERROR.val(),"支付失败！");
        }
    }

    @RequestMapping(value = "/getOrderListAdmin",method = RequestMethod.POST)
    public Result getOrderListAdmin(@RequestParam(required = false) String order_id,@RequestParam(required = false) String startTime,
                                    @RequestParam(required = false) String endTime,@RequestParam(required = false) Integer is_pay,
                                    @RequestParam(defaultValue = "10") Integer pageSize,@RequestParam(defaultValue = "1") Integer pageNum){
        Order order = new Order();
        order.setOrder_id(order_id);
        order.setIs_pay(is_pay);
        PageInfo<Order> orderPageInfo = null;
        try{
            orderPageInfo = PageHelper.startPage(pageNum,pageSize).doSelectPageInfo(()->this.orderServices.getOrderListAdmin(order,startTime,endTime));
        }catch (Exception e){
            e.printStackTrace();
        }
        if(orderPageInfo!=null){
            return Result.success(orderPageInfo,"获取订单列表成功！");
        }else{
            return Result.fail(ResponseCode.ERROR.val(),"获取订单列表失败！");
        }
    }

    @RequestMapping(value = "/getStatics",method = RequestMethod.POST)
    public Result getStatic(@RequestParam(required = false) String startTime,@RequestParam(required = false) String endTime,
                            @RequestParam(required = false) String goods_name,@RequestParam(defaultValue = "10") Integer pageSize,
                            @RequestParam(defaultValue = "1") Integer pageNum){
        PageInfo<OrderDetail> orderDetailPageInfo = null;
        try{
            orderDetailPageInfo = PageHelper.startPage(pageNum,pageSize).doSelectPageInfo(()->this.orderServices.getOrderStatics(startTime, endTime, goods_name));
        }catch (Exception e){
            e.printStackTrace();
        }
        if(orderDetailPageInfo!=null){
            return Result.success(orderDetailPageInfo,"获取统计数据成功!");
        }else{
            return Result.fail(ResponseCode.ERROR.val(),"获取统计数据失败！");
        }
    }
}
