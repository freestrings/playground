package fs.rest;

import fs.theother.TheOtherEntityRepository;
import fs.one.OneEntity;
import fs.one.OneEntityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EntityService {

    @Autowired
    private OneEntityRepository oneEntityRepository;

    @Autowired
    private TheOtherEntityRepository theOtherEntityRepository;

    public OneEntity one(long id) {
        return oneEntityRepository.findOne(id);
    }

    public OneEntity insert(String name) {
        return oneEntityRepository.saveAndFlush(new OneEntity(name));
    }
}
