package br.com.avelar.weatherlogger.controller;

import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.core.Authentication;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import br.com.avelar.weatherlogger.acl.ACLPermissions;
import br.com.avelar.weatherlogger.data.WeatherData;
import br.com.avelar.weatherlogger.data.WeatherDataService;
import br.com.avelar.weatherlogger.data.WeatherDataValidator;
import br.com.avelar.weatherlogger.helpers.HttpHeadersHelper;

@RestController
@RequestMapping("/weather")
public class WeatherDataController {

  private WeatherDataService weatherDataService;

  @Autowired
  private WeatherDataValidator weatherDataValidator;
  
  @Autowired
  private ACLPermissions permissions;

  @Autowired
  public WeatherDataController(WeatherDataService weatherDataService) {
    this.weatherDataService = weatherDataService;
  }

  @InitBinder("weatherData")
  public void initBinder(WebDataBinder binder) {
    binder.addValidators(weatherDataValidator);
  }

  @CrossOrigin
  @RequestMapping(method = RequestMethod.POST)
  public ResponseEntity<Void> saveData(@Valid @RequestBody WeatherData data, 
                                                           Errors errors, 
                                                           HttpHeadersHelper httpHeadersHelper,
                                                           Authentication authentication) {
    HttpHeaders headers = null;

    if (errors.hasErrors()) {
      return new ResponseEntity<Void>(HttpStatus.BAD_REQUEST);
    }

    if (data.getId() != null) {
      headers = httpHeadersHelper.addLocationHeader("/weather", data.getId());
      return new ResponseEntity<Void>(headers, HttpStatus.CONFLICT);
    }

    WeatherData savedData = weatherDataService.save(data);
    headers = httpHeadersHelper.addLocationHeader("/weather", savedData.getId());
    permissions.add(authentication, savedData, BasePermission.READ, BasePermission.CREATE, 
                                                BasePermission.DELETE, BasePermission.WRITE);
    return new ResponseEntity<Void>(headers, HttpStatus.CREATED);
  }

  @CrossOrigin
  @PreAuthorize("hasPermission(#id, 'br.com.avelar.weatherlogger.data.WeatherData', 'read')")
  @RequestMapping(value = "/{id}", method = RequestMethod.GET)
  public ResponseEntity<WeatherData> findData(@PathVariable Long id) {
    WeatherData data = weatherDataService.findOne(id);

    if (data == null) {
      return new ResponseEntity<WeatherData>(HttpStatus.NOT_FOUND);
    }

    return new ResponseEntity<WeatherData>(data, HttpStatus.OK);
  }

  @CrossOrigin
  @PreAuthorize("hasPermission(#id, 'br.com.avelar.weatherlogger.data.WeatherData', 'delete')")
  @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
  public ResponseEntity<Void> deleteData(@PathVariable Long id) {
    WeatherData data = weatherDataService.findOne(id);

    if (data == null) {
      return new ResponseEntity<Void>(HttpStatus.NOT_FOUND);
    }

    weatherDataService.delete(data);
    return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
  }

}
