package jpabook.jpashop.service;

import jpabook.jpashop.domain.*;
import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.exception.NotEnoughStockException;
import jpabook.jpashop.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class OrderServiceTest {

    @PersistenceContext
    EntityManager em;

    @Autowired OrderService orderService;
    @Autowired OrderRepository orderRepository;

    @Test
    public void 상품주문() throws Exception{

        //Given
        Member member = createMember();
        Item item = createBook("시골 JPA", 10000, 10); // 이름, 가격, 재고

        int orderCount = 2;
        //When
        Long orderId =orderService.order(member.getId(), item.getId(), orderCount);

        //Then
        Order getOrder = orderRepository.findOne(orderId);

        // 상품 주문시 상태 확인
        assertThat(getOrder.getStatus()).isEqualTo(OrderStatus.ORDER);
        assertEquals(OrderStatus.ORDER, getOrder.getStatus(),"상품 주문시 상태는 ORDER");
        // 주문한 상품 종류수 확인
        assertThat(getOrder.getOrderItems().size()).isEqualTo(1);
        // 주문 가격은 가격 * 수량
        assertThat(getOrder.getTotalPrice()).isEqualTo(10000 * 2);
        // 주문 수량만큼 재고 수 down
        assertThat(item.getStockQuantity()).isEqualTo(8);
    }

    @Test
    public void 상품주문_재고수량초과() throws Exception{

        //given
        Member member = createMember();
        Item item = createBook("Test", 20000, 10);

        int orderCount = 11;

        //when
        NotEnoughStockException thrown = assertThrows(NotEnoughStockException.class,
                ()->orderService.order(member.getId(), item.getId(), orderCount));

        //then
        assertThat(thrown.getMessage()).isEqualTo("재고가 모자랍니다");
    }

    @Test
    public void 주문취소(){
        //Given
        Member member = createMember();
        Item item = createBook("Test", 20000, 10);

        int orderCount = 2;

        Long orderId = orderService.order(member.getId(), item.getId(), orderCount);

        //When
        orderService.cancelOrder(orderId);

        //Then
        Order getOrder = orderRepository.findOne(orderId);

        assertThat(getOrder.getStatus()).isEqualTo(OrderStatus.CANCEL);
        assertThat(item.getStockQuantity()).isEqualTo(10);

    }

    private Item createBook(String name, int price, int stockQuantity) {
        Book book = new Book();
        book.setName(name);
        book.setPrice(price);
        book.setStockQuantity(stockQuantity);

        em.persist(book);
        return book;
    }

    private Member createMember() {
        Member member = new Member();
        member.setName("회원1");
        member.setAddress(new Address("대구","달서구","704-708"));
        em.persist(member);
        return member;
    }
}
