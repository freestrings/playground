package fs;

import fs.one.model.User;
import fs.one.repository.UserRepository;
import fs.two.model.Company;
import fs.two.repository.CompanyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Component
public class FsTransactionService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CompanyRepository companyRepository;

    public void saveUser() {
        User user = new User();
        user.setId(1);
        user.setName("han");
        userRepository.save(user);
    }

    public void saveCompany() {
        Company company = new Company();
        company.setId(1);
        company.setName("freestrings");
        companyRepository.save(company);
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveUserWithError() throws Exception {
        saveUser();
        throw new Exception("force error");
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public void saveUserOnRequireNewWithError() throws Exception {
        saveUser();
        throw new Exception("force error");
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveUserOnRequireNew() throws Exception {
        saveUser();
    }

    @Transactional
    public void save() {
        saveCompany();
        saveUser();
    }

    @Transactional
    public void saveWithError() {
        saveCompany();
        try {
            saveUserWithError();
        } catch (Exception e) {
        }
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public void saveOnRequireNewWithError() throws Exception {
        saveCompany();
        try {
            saveUserOnRequireNewWithError();
        } catch (Exception e) {
            throw e;
        }
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public void saveOnRequireNewWithError2() throws Exception {
        saveCompany();
        saveUserOnRequireNew();
        throw new Exception("force error");
    }

    public List<?> findAll() {
        List<CrudRepository<?, Integer>> repositories = Arrays.asList(userRepository, companyRepository);
        return repositories.stream()
                .map(r -> r.findAll())
                .flatMap(r -> StreamSupport.stream(r.spliterator(), false))
                .collect(Collectors.toCollection(ArrayList::new));

    }

}
