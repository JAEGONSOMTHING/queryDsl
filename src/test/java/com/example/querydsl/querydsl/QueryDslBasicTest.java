package com.example.querydsl.querydsl;

import com.example.querydsl.entity.*;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.io.Serializable;
import java.util.List;

import static com.example.querydsl.entity.QBoard.board;
import static com.example.querydsl.entity.QUser.user;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
public class QueryDslBasicTest {

    @Autowired
    EntityManager em;
    JPAQueryFactory queryFactory;


    @BeforeEach
    public void init() {
        queryFactory = new JPAQueryFactory(em);
        for (int i = 0; i < 5; i++) {
            User user = new User("user" + i);
            em.persist(user);
            Board board1 = new Board("title" + (2*i), "content" + (2*i), user);
            Board board2 = new Board("title" + (2*i+1),"content" + (2*i+1),user);
            em.persist(board1);
            em.persist(board2);
            Comment comment = new Comment("coment",user,board1);
            em.persist(comment);

        }
        em.flush();
        em.clear();
        System.out.println("==============init===============");
    }

    @Test
    void findTest() {

        User findUser = queryFactory.
                select(user)
                .from(user)
                .where(user.name.eq("user1"))
                .fetchOne();



        assertThat(findUser.getName()).isEqualTo("user1");
    }

    @Test
    @DisplayName("여러 조건의 where")
    void whereTest() {

        List<User> find = queryFactory.select(user)
                .from(user)
                .where(
                        user.name.startsWith("user")
                                .and(user.name.endsWith("3"))
                )
                .fetch();

        assertThat(find).size().isEqualTo(1);

    }

    @Test
    @DisplayName("fetch")
    void fetchTest() {
        //.fetch() 모든 결과
        List<User> fetch = queryFactory.selectFrom(user)
                .fetch();// 모든 결과
        assertThat(fetch).size().isEqualTo(5);

        // .fetchOne() 결과가 0이면 null 많으면 error
        User fetchOne = queryFactory.selectFrom(user)
                .where(user.name.eq("notuser"))
                .fetchOne();
        assertThat(fetchOne).isNull();
        assertThrows(RuntimeException.class,
                () -> queryFactory.selectFrom(user).fetchOne());

        // .fetchFirst ==limit(1).fetchOne()
        User fetchFirst = queryFactory.selectFrom(user)
                .fetchFirst();

        //count
        int size = queryFactory.selectFrom(user)
                .fetch().size();
        assertThat(size).isEqualTo(5);

    }

    @Test
    @DisplayName("정렬")
    void sortTest(){
        em.persist(new User(null));
        //asc(), desc() , nullsLast(), nullsFirst()
        List<User> users = queryFactory.selectFrom(user)
                .orderBy(user.name.asc().nullsLast())
                .fetch();
        assertThat(users.get(0).getName()).isEqualTo("user0");
        int size = users.size();
        assertThat(users.get(size-1).getName()).isNull();

    }

    @Test
    @DisplayName("페이징")
    void paging(){
        List<User> users = queryFactory.selectFrom(user)
                .orderBy(user.name.desc())
                .offset(2)
                .limit(2)
                .fetch();
        assertThat(users.size()).isEqualTo(2);
        assertThat(users.get(0).getName()).isEqualTo("user2");
        assertThat(users.get(1).getName()).isEqualTo("user1");

        Long totalCount = queryFactory.select(user.count())
                .from(user)
                .fetchOne();

        assertThat(totalCount).isEqualTo(5L);
    }

    @Test
    @DisplayName("조인")
    void groupByTest(){
        List<Board> boards = queryFactory
                .select(board)
                .from(board)
                .join(board.user, user)
                .where(user.name.eq("user1"))
                .fetch();

        assertThat(boards.size()).isEqualTo(2);

    }
    @Test
    void dynamicSortingOption(){



        List<Board> fetch = queryFactory
                .select(board)
                .from(board)
                .orderBy(

                )
                .fetch();

        fetch.forEach(b-> System.out.println(b.getTitle()));


    }

    private OrderSpecifier<String> userNameAsc() {
        return board.user.name.asc();
    }

    private OrderSpecifier<Integer> commentCountDesc() {
        return board.comments.size().desc();
    }

    @Test
    void listTest(){

        System.out.println(List.of("1","2","3").getClass());

    }


}
