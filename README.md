# MyBatis数据检查插件 #

## 前言: ##

在开发中一般存在数据唯一性检查，如用户登陆名。我们在写代码之时通常会写如下代码，如果每次都这样写，则过于繁琐，本插件便简化类似于此种需求的

```java

User user userMapper.selectUserByUserName(userName);
if(user!=null){
	throw new BadRequestException("用户名重复")
}
```

----------

## Maven引用：



```java
<dependency>
    <groupId>com.github.XujinquanGitHub</groupId>
    <artifactId>data-check</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

------



### 1. 使用方式： ###

##### 1.传统Spring配置文件方式
	    <plugins>
	        <plugin interceptor="github.datacheck.DuplicateDataInterceptor">
	            <!-- modelPackages 为数据对应Model所在包，多个包时用，隔开 -->
	            <property name="modelPackages" value="github.datacheck.model"/>
	        </plugin>
	    </plugins>
##### 2.Spring Boot方式
    @Configuration
    public class CommonBeans {
        @Bean
        public DuplicateDataInterceptor interceptor() {
            DuplicateDataInterceptor interceptor = new DuplicateDataInterceptor();
            Properties props = new Properties();
            props.setProperty("modelPackages", "github.datacheck.model");
            locker.setProperties(props);
            return interceptor;
        }
    }

----------

### 2. Model的使用： ###

在需要检查的Model字段上加上DuplicateData注解，例如：

	@Data
	@Accessors(chain = true)
	public class User implements Serializable {
	
	    private Long id;
	
	    @DuplicateData(template = "名字重复！")
	    private String name;
	
	    private Integer age;
	
	    @DuplicateData(template = "邮箱重复！")
	    private String email;
	}

----------

### 3. 关于作者： ###
	作者微信：xu1304784755
	作者邮箱：xujinquan-main@qq.com
