package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;
import ru.practicum.shareit.user.User;

import javax.persistence.TypedQuery;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@DataJpaTest
@TestPropertySource(locations = "classpath:application-test.properties")
class ItemRepositoryTest {
    @Autowired
    private TestEntityManager em;

    @Test
    void verifyRepositoryBySearchAvailableItems() {
        Item item = new Item("Thing", "Cool thing", true, 0);
        item.setId(1L);
        Item item2 = new Item("Thing2", "Cool thing2", true, 0);
        item2.setId(2L);
        User user = new User(1L, "John", "item@mail.ru");
        item.setOwner(user);
        item2.setOwner(user);
        em.merge(user);
        em.merge(item);
        TypedQuery<Item> query = em.getEntityManager().createQuery("select i from Item as i " +
                "where (upper(i.name) like upper(concat('%', :text, '%')) " +
                "or upper(i.description) like upper(concat('%', :text, '%'))) " +
                "and i.isAvailable = true", Item.class);
        List<Item> availableItems = query.setParameter("text", "thing").getResultList();
        assertThat(availableItems, hasSize(1));
        for (Item item1 : availableItems) {
            assertThat(item1.getId(), equalTo(item.getId()));
            assertThat(item1.getName(), equalTo(item.getName()));
            assertThat(item1.getDescription(), equalTo(item.getDescription()));
            assertThat(item1.getIsAvailable(), equalTo(item.getIsAvailable()));
            assertThat(item1.getOwner().getId(), equalTo(item.getOwner().getId()));
            assertThat(item1.getLastBooking(), nullValue());
            assertThat(item1.getNextBooking(), nullValue());
            assertThat(item1.getComments(), hasSize(0));
        }
    }
}