package fs.rest;

import fs.one.OneEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/v1")
public class V1 {

    @Autowired
    private EntityService entityService;

    @RequestMapping("/one/{id}")
    public Map<String, Object> getOne(@PathVariable long id) {
        return oneEntityToMap(entityService.one(id));
    }

    @RequestMapping(value = "/one", method = RequestMethod.POST)
    public Map<String, Object> putOne(@RequestBody String name) {
        return oneEntityToMap(entityService.insert(name));
    }

    private Map<String, Object> oneEntityToMap(OneEntity e) {
        HashMap<String, Object> ret = new HashMap<>();
        ret.put("id", e.getId());
        ret.put("name", e.getName());
        return ret;
    }
}
