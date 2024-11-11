package com.project.shopapp.service.impl;

import com.project.shopapp.components.LocalizationUtils;
import com.project.shopapp.dto.OrderDetailDTO;
import com.project.shopapp.exceptions.DataNotFoundException;
import com.project.shopapp.mapper.OrderDetailMapper;
import com.project.shopapp.models.Order;
import com.project.shopapp.models.OrderDetail;
import com.project.shopapp.models.Product;
import com.project.shopapp.repositories.OrderDetailRepository;
import com.project.shopapp.repositories.OrderRepository;
import com.project.shopapp.repositories.ProductRepository;
import com.project.shopapp.service.IOrderDetailService;
import com.project.shopapp.untils.MessageKeys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
@RequiredArgsConstructor
@Service
public class OrderDetailServiceImpl implements IOrderDetailService {
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final LocalizationUtils localizationUtils;
    @Override
    @Transactional
    public OrderDetail createOrderDetail(OrderDetailDTO orderDetailDTO) throws DataNotFoundException {
        //tim em orderId co ton tai khong
        Order order = orderRepository.findById(orderDetailDTO.getOrderId())
                .orElseThrow(()-> new DataNotFoundException(
                        localizationUtils.getLocalizeMessage(MessageKeys.NOT_FOUND_ORDER, orderDetailDTO.getOrderId())));
        Product product = productRepository.findById(orderDetailDTO.getProductId()).orElseThrow(
                () -> new DataNotFoundException(
                        localizationUtils.getLocalizeMessage(MessageKeys.NOT_FOUND_PRODUCT, orderDetailDTO.getProductId())));

        OrderDetail orderDetail = OrderDetail.builder()
                .order(order)
                .product(product)
                .price(orderDetailDTO.getPrice())
                .totalMoney(orderDetailDTO.getTotalMoney())
                .numberOfProduct(orderDetailDTO.getNumberOfProducts())

                .build();

        return orderDetailRepository.save(orderDetail);
    }


    @Override
    public OrderDetail getOrderDetail(Long id) throws DataNotFoundException {
        return orderDetailRepository.findById(id).orElseThrow(
                ()->new DataNotFoundException(localizationUtils.getLocalizeMessage(MessageKeys.NOT_FOUND_ORDER_DETAIL,id)));
    }

    @Override
    @Transactional
    public OrderDetail updateOrderDetail(Long id, OrderDetailDTO orderDetailDTO) throws DataNotFoundException {
        //tim xem co ton tai khong
        OrderDetail existingOrderDetail = orderDetailRepository.findById(id).orElseThrow(
                () -> new DataNotFoundException(
                        localizationUtils.getLocalizeMessage(MessageKeys.NOT_FOUND_ORDER_DETAIL,id))
        );
        Order existingOrder = orderRepository.findById(orderDetailDTO.getOrderId())
                .orElseThrow(() -> new DataNotFoundException(
                        localizationUtils.getLocalizeMessage(MessageKeys.NOT_FOUND_ORDER, orderDetailDTO.getOrderId())));
        Product existingProduct = productRepository.findById(orderDetailDTO.getProductId())
                .orElseThrow(() -> new DataNotFoundException(
                        localizationUtils.getLocalizeMessage(MessageKeys.NOT_FOUND_PRODUCT, orderDetailDTO.getProductId())));
        OrderDetailMapper.MAPPER.updateOrderDetailFromDto(orderDetailDTO,existingOrderDetail);
        existingOrderDetail.setOrder(existingOrder);
        existingOrderDetail.setProduct(existingProduct);
        return existingOrderDetail;
    }

    @Override
    @Transactional
    public void deleteOrderDetail(Long id) {
        orderDetailRepository.deleteById(id);
    }

    @Override
    public List<OrderDetail> findByOrderId(Long orderId) {
        List<OrderDetail> orderDetails = orderDetailRepository.findByOrderId(orderId);
        return orderDetails;
    }
}
