package site.yemei.photoApp.api.users.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;



import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


import feign.FeignException;
import net.bytebuddy.asm.Advice.This;
import site.yemei.photoApp.api.users.data.AlbumsServiceClient;
import site.yemei.photoApp.api.users.data.UserEntity;
import site.yemei.photoApp.api.users.data.UserRepository;
import site.yemei.photoApp.api.users.shared.UserDto;
import site.yemei.photoApp.api.users.ui.model.AlbumResponseModel;

@Service
public class UsersServiceImpl implements UserService {

	UserRepository userRepository;
	BCryptPasswordEncoder bCryptPasswordEncoder;
	//RestTemplate restTemplate;
	Environment environment;
	AlbumsServiceClient albumsServiceClients;
	Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	public UsersServiceImpl(UserRepository userRepository, 
							BCryptPasswordEncoder bCryptPasswordEncoder,
							AlbumsServiceClient albumsServiceClients,
							Environment environment) {
		
		this.userRepository = userRepository;
		this.bCryptPasswordEncoder = bCryptPasswordEncoder;
		//this.restTemplate = restTemplate;
		this.environment = environment;
		this.albumsServiceClients = albumsServiceClients;
	}

	@Override
	public UserDto CreateUser(UserDto userDetails) {

		userDetails.setUserId(UUID.randomUUID().toString());
		userDetails.setEncryptedPassword(bCryptPasswordEncoder.encode(userDetails.getPassword()));

		ModelMapper modelMapper = new ModelMapper();
		modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

		UserEntity userEntity = modelMapper.map(userDetails, UserEntity.class);

		userRepository.save(userEntity);

		UserDto returnValue = modelMapper.map(userEntity, UserDto.class);

		return returnValue;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

		UserEntity userEntity = userRepository.findByEmail(username);

		if (userEntity == null)
			throw new UsernameNotFoundException(username);

		return new User(userEntity.getEmail(), userEntity.getEncryptedPassword(), true, true, true, true, new ArrayList<>());
	}

	@Override
	public UserDto getUserDetailsByEmail(String email) {

		UserEntity userEntity = userRepository.findByEmail(email);

		if (userEntity == null)
			throw new UsernameNotFoundException(email);

		return new ModelMapper().map(userEntity, UserDto.class);
	}

	@Override
	public UserDto getUserByUserId(String userId) {
		
		UserEntity userEntity = userRepository.findByUserId(userId);

		if (userEntity == null)
			throw new UsernameNotFoundException("User not found");

		UserDto userDto = new ModelMapper().map(userEntity, UserDto.class);

//		String albumsUrl = String.format(environment.getProperty("albums.url"), userId);
//		
//		ResponseEntity<List<AlbumResponseModel>> albumsListResponse = restTemplate.exchange(albumsUrl, 
//																							HttpMethod.GET, 
//																							null, 										
//																							new ParameterizedTypeReference<List<AlbumResponseModel>>(){});
//        List<AlbumResponseModel> albumsList = albumsListResponse.getBody(); 
        
//		List<AlbumResponseModel> albumsList = null;
//		try {
//			albumsList = albumsServiceClients.getAlbums(userId);
//		} catch (FeignException e) {
//			logger.error(e.getLocalizedMessage());
//		}
		
		logger.info("Before calling albums Microservice");
		List<AlbumResponseModel> albumsList = albumsServiceClients.getAlbums(userId);
		logger.info("After calling albums Microservice");
		userDto.setAlbums(albumsList);

		return userDto;
	}

}
