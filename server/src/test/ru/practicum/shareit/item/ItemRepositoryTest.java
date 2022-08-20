package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;
import ru.practicum.shareit.item.Item;
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
        Item item1 = new Item("ThingNeeded", "Cool thing1", true, 0);
        item1.setId(1L);
        Item item2 = new Item("Thing2", "Cool thing2", true, 0);
        item2.setId(2L);
        User user = new User(1L, "John", "item@mail.ru");
        item1.setOwner(user);
        item2.setOwner(user);
        em.merge(user);
        em.merge(item1);
        em.merge(item2);
        TypedQuery<Item> query = em.getEntityManager().createQuery("select i from Item as i " +
                "where (upper(i.name) like upper(concat('%', :text, '%')) " +
                "or upper(i.description) like upper(concat('%', :text, '%'))) " +
                "and i.isAvailable = true", Item.class);
        List<Item> availableItems = query.setParameter("text", "needed").getResultList();
        assertThat(availableItems, hasSize(1));
        for (Item item3 : availableItems) {
            assertThat(item3.getId(), equalTo(item1.getId()));
            assertThat(item3.getName(), equalTo(item1.getName()));
            assertThat(item3.getDescription(), equalTo(item1.getDescription()));
            assertThat(item3.getIsAvailable(), equalTo(item1.getIsAvailable()));
            assertThat(item3.getOwner().getId(), equalTo(item1.getOwner().getId()));
            assertThat(item3.getLastBooking(), nullValue());
            assertThat(item3.getNextBooking(), nullValue());
            assertThat(item3.getComments(), hasSize(0));
        }
    }
}