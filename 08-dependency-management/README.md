# 8장. 의존성 관리하기

협력을 위해서는 의존성이 필요하지만 과도한 의존성은 애플리케이션을 수정하기 어렵게 만든다. 객체지향 설계의 핵심은 협력을 위해 필요한 의존성은 유지하면서도 변경을 방해하는 의존성은 제거하는데 있다.

## 1. 의존성 이해하기

### **변경과 의존성**

어떤 객체가 협력하기 위해 다른 객체를 필요로 할 때 두 객체 사이에 의존성이 존재하게 된다.

- 실행 시점
    - 의존하는 객체가 정상적으로 동작하기 위해서는 실행 시에 의존 대상 객체가 반드시 존재해야 한다.
- 구현 시점
    - 의존 대상 객체가 변경될 경우 의존하는 객체도 함께 변경된다.

```java
public class PeriodCondition implements DiscountCondition {
	private DayOfWeek dayOfWeek;
	private LocalTime startTime;
	private LocalTime endTime;
	...

	public boolean isSatisfiedBy(Screening screening) {
		return screening.getStartTime().getDayOfWeek().equals(dayOfWeek) &&
			startTime.compareTo(screening.getStartTime().toLocalTime()) <= 0 &&
			endTime.compareTo(screening.getEndTime().toLocalTime()) >= 0;	
	}
}
```

위 예시의 PeriodCondition 클래스는 DayOfWeek, LocalTime, Screening, DiscountCondition에 의존하고 있다.

[그림8-2]

### **의존성 전이(Transitive Dependency)**

의존성 전이의 의미는 PeriodCondition이 Screening에 의존할 경우 PeriodCondition은 Screening이 의존하는 대상에 대해서도 자동적으로 의존하게 된다는 것이다.

의존성은 함께 변경될 수 있는 가능성을 의미하기 때문에 모든 경우에 의존성이 전이되는 것은 아니다. 의존성은 실제로 전이될지 여부는 변경의 방향과 캡슐화의 정도에 따라 달라진다.

- 직접 의존성(Direct Dependency)
    - 한 요소가 다른 요소에 직접 의존하는 경우
    - PeriodCondition이 Screening에 의존하는 경우가 여기에 속함
    - 의존성은 PeriodCondition 코드에 명시적으로 드러남
- 간접 의존성(Indirect Dependency)
    - 직접적인 관계는 존재하지 않지만 의존성 전이에 영향이 전파되는 경우
    - 코드에 직접적으로 드러나지 않음

### 런타임 의존성과 컴파일타임 의존성

- 런타임 의존성(Run-time Dependency)
    - 애플리케이션이 실행되는 시점
    - 객체 사이의 의존성
- 컴파일타임 의존성(Compile-time Dependency)
    - 작성된 코드를 컴파일하는 시점
    - 클래스 사이의 의존성

[그림8-5]

Movie 클래스에서 AmountDiscountPolicy 클래스와 PercentDiscountPolicy 클래스로 향하는 어떤 의존성도 존재하지 않는다. 오직 추상 클래스인 DiscountPolicy만 의존한다.

하지만 런타임 의존성을 살펴보면 상황이 완전히 달라진다. 두 인스턴스와 모두 협력할 수 있어야 한다.

Movie 클래스가 두 클래스를 의존하도록 만들게 되면 결합도를 높일뿐만 아니라 새로운 할인 정책을 추가하기 어렵게 된다.

추상 클래스에 의존하도록 만들고 이 컴파일타임 의존성을 런타임 의존성으로 대체해야 한다.

### 컨텍스트 독립성

클래스가 특정한 문맥에 강하게 결합될수록 다른 문맥에서 사용하기는 더 어려워진다. 클래스가 사용될 특정한 문맥에 대해 최소한의 가정만으로 이뤄져 있다면 다른 문맥에서 재사용하기가 더 수월해진다. 이를 컨텍스트 독립성이라 부른다.

가능한 한 자신이 실행될 컨텍스트에 대한 구체적인 정보를 최대한 적게 알아야 한다. 컨텍스트에 대한 정보가 적을수록 더 다양한 컨텍스트에서 재사용될 수 있기 때문이다.

### 의존성 해결하기

컴파일타임 의존성을 실행 컨텍스트에 맞는 적잘한 런타임 의존성으로 교체하는 것을 의존성 해결이라 한다.

- 객체를 생성하는 시점에 생성자를 통해 의존성 해결
- 객체 생성 후 setter 메서드를 통해 의존성 해결
- 메서드 실행 시 인자를 이용해 의존성 해결

```java
// 생성자를 통해 의존성 해결
Movie avatar = new Movie("아바타",
		Duration.OfMinutes(120),
		Money.wons(10000),
		new AmountDiscountPolicy(...));

// setter 메서드를 통해 의존성 해결
Movie avatar = new Movie(...);
avatar.setDiscountPolicy(new AmountDiscountPolicy(...));

// 단, setter 메서드를 이용해 인스턴스를 불완전 상태로 생성할 경우 주의해야함
Movie avatar = new Movie(...);
avatar.calculateFee(...); // nullPointerException 예외 발생!
avatar.setDiscountPolicy(new AmountDiscountPolicy(...));

// 더 좋은 방법은 혼합하여 사용
Movie avatar = new Movie("아바타",
		Duration.OfMinutes(120),
		Money.wons(10000),
		new AmountDiscountPolicy(...));
avatar.setDiscountPolicy(new PercentDiscountPolicy(...));

// 메서드 실행 인자로 해결
public class Movie {
	...
	public Money calculateMovieFee(..., DiscountPolicy discountPolicy) {
			...
}
```

## 2. 유연한 설계

### 의존성과 결합도

객체지향 패러다임의 근간은 협력이다. 모든 의존성이 나쁜 것은 아니다.

Movie가 비율 할인 정책을 구현하는 PercentDiscountPolicy에 직접 의존한다고 가정해보자.

```java
public class Movie {
	...
	private PercentDiscountPolicy percentDiscountPolicy;
	...
}
```

둘 사이의 의존성이 존재하는 것은 문제가 아니다. 문제는 의존성의 존재가 아니라 의존성의 정도이다. 이 코드는 다른 종류의 할인 정책이 필요한 문맥에서 Movie를 재사용할 수 있는 가능성을 없애 버렸다. 해결 방법은 의존성을 바람직하게 만드는 것이다.

그렇다면 바람직한 의존성은 무엇인가?

어떤 의존성이 다양한 환경에서 클래스를 재사용할 수 없도록 제한한다면 그 의존성은 바람직하지 못한 것이다.

어떤 의존성이 다양한 환경에서 재사용할 수 있다면 그 의존성은 바람직한 것이다.

다시 말해 독립적인 의존성은 바람직한 의존성이고 특정한 컨텍스트에 강하게 결합된 의존성은 바람직하지 않은 의존성이다.

### 지식이 결합을 낳는다.

Movie 클래스가 추상 클래스인 DiscountPolicy 클래스에 의존하는 경우 구체적인 계산 방법은 알 필요가 없다. 그저 할인 요금을 계산한다는 사실만 알고 있을 뿐이다. 따라서 Movie가 PercentDiscountPolicy에 의존하는 것보다 DiscountPolicy에 의존하는 경우 알아야 하는 지식의 양이 적기 때문에 결합도가 느슨해지는 것이다.

### 추상화에 의존하라

추상화란 어떤 양상, 세부사항, 구조를 좀 더 명확하게 이해하기 위해 특정 절차나 물체를 읮도적으로 생략하거나 감춤으로써 복잡도를 극복하는 방법이다. 필요한 정보를 감추기 때문에 지식의 양을 줄일 수 있다.

클라이언트가 알아야 할 지식의 양이 적은 순서(결합도가 느슨한 순서)

1. 인터페이스 의존성(interface dependency)
2. 추상 클래스 의존성(abstract class dependency)
3. 구체 클래스 의존성(concrete class dependency): 부모 자식 관계

### 명시적 의존성

의존성의 대상을 생성자의 인자로 전달받는 방법과 생성자 안에서 직접 생성하는 방법의 차이점은 퍼블릭 인터페이스를 통해 할인 정책을 설정할 수 잇는 방법을 제공하는지 여부다.

생성자의 인자로 Movie가 DiscountPolicy에 의존한다는 사실을 Movie의 퍼블릭 인터페이스에 드러내는 것이다. 이를 명시적인 의존성(explicit dependency)라고 부른다.

반면 Movie 내부에서 AmountDiscountPolicy 인스턴스를 직접 생성하는 방식은 Movie가 DiscountPolicy에 의존한다는 사실을 감춘다. 이를 숨겨진 의존성(hidden dependency)이라 한다.

의존성이 명시적이지 않으면 의존성을 파악하기 위해 내부 구현을 직접 살펴볼 수 밖에 없다.

의존성이 명시적이지 않으면 클래스를 다른 컨텍스트에서 재사용하기 위해 내부 구현을 직접 변경해야 한다는 것이다. 

클래스가 다른 클래스를 의존하는 것은 부끄러운 일이 아니다. 의존성은 다른 객체와의 협력을 가능하게 해주기 때문에 바람직한 것이다. 경계해야 할 것은 의존성 자체가 아니라 의존성을 감추는 것이다.