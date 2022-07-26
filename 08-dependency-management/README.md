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

![[그림8-2]](https://github.com/swimming-lab/study-java-object/raw/master/08-dependency-management/8-2.png)


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

![[그림8-5]](https://github.com/swimming-lab/study-java-object/raw/master/08-dependency-management/8-5.png)

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

### new는 해롭다

- new 연산자를 사용하기 위해서는 구체 클래스의 이름을 직접 기술해야 한다. 따라서 new를 사용하는 클라이언트는 추상화가 아닌 구체 클래스에 의존할 수밖에 없기 때문에 결합도가 높아진다.
- new 연산자는 생성하려는 구체 클래스뿐만 아니라 어떤 인자를 이용해 클래스의 생성자를 호출해야 하는지도 알아야 한다. 따라서 new를 사용하면 클라이언트가 알아야 하는 지식의 양이 늘어나기 때문에 결합도가 높아진다.

해결 방법은 인스턴스를 생성하는 로직과 생성된 인스턴스를 사용하는 로직을 분리하는 것이다.

AmountDiscountPolicy를 사용하는 Movie는 인스턴스를 생성해서는 안 된다. 단지 해당하는 인스턴스를 외부로부터 전달받아 사용해야 한다.

```java
// 개선 전
public class Movie {
	...
	public Movie(...) {
		...
		this.discountPolicy = new AmountDiscountPolicy(...);
	}
}

// 개선 후
public class Movie {
	...
	public Movie(..., DiscountPolicy discountPolicy) {
		...
		this.discountPolicy = discountPolicy;
	}
}
```

사용과 생성의 책임을 분리하고, 의존성을 생성자에 명시적으로 드러내고, 구체 클래스가 아닌 추상 클래스에 의존하게 함으로써 설계를 유연하게 만들 수 있다. 그리고 그 출발은 객체를 생성하는 책임을 객체 내부가 아니라 클라이언트로 옮기는 것에서 시작한다.

### 가끔은 생성해도 무방하다

클래스 안에서 객체의 인스턴스를 직접 생성하는 방식이 유용한 경우도 있다. 주로 협력하는 기본 객체를 설정하고 싶은 경우가 여기에 속한다.

예를 들어 Movie가 대부분의 경우 AmountDiscountPolicy의 인스턴스와 협력한다면 인스턴스 생성을 클라이언트에서만 책임진다면 중복 코드가 늘어나고 Movie 사용성도 나빠질 것이다. 생성자 체이닝 기법을 사용해 의도적으로 생성자를 추가할 수 있다.

```java
// 의도적으로 생성자를 추가하여 클라이언트의 중복 코드를 제거한다.
public class Movie {
	...
	public Movie(String title, Duration runningTime) {
		this(title, runningTime, new AmountDiscountPolicy(...));
	}

	public Movie(..., DiscountPolicy discountPolicy) {
		...
		this.discountPolicy = discountPolicy;
	}
}
```

그럼에도 가급적 구체 클래스에 대한 의존성을 제거할 수 있는 방법을 찾아봐야 한다.

### 표준 클래스에 대한 의존성은 해롭지 않다.

의존성이 불편한 이유는 그것이 항상 변경에 대한 영향을 암시하기 때문이다. 변경될 확률이 거의 없는 클래스라면 의존성이 문제가 되지 않는다.

예를 들어 JDK에 포함된 표준 클래스가 이 부류에 속한다.

new ArrayList와 같은 생성자는 수정될 확률이 0에 가깝기 때문에 직접 생성하더라도 문제가 되지 않는다.

### 컨텍스트 확장하기

Movie객체가 유연하다는 사실을 입증하기 위해 두 가지 예를 살펴본다.

1. 할인 혜택을 제공하지 않는 경우
    1. Movie에서 DiscountPolicy 인스턴스를 Null값을 주지 않고
    2. 할인을 하지 않은 정책 클래스를 동일하게 작성한다.

```java
public class NoneDiscountPolicy extends DiscountPolicy {
	@Override
	protected Money getDiscountAmount(Srcreening screening) {
		return Money.ZERO;
	}
}
```

1. 중복 적용이 가능한 할인 정책을 추가할 경우
    1. List로 구성된 DiscountPolicy를 여러가 받아야 한다.
    2. Movie의 멤버변수 discountPolicy의 타입을 List로 바꾸지 않고
    3. 동일한 방법으로 여러 할인 정책을 수행할 수 있는 정책 클래스를 작성한다.

```java
public class OverlappedDiscountPolicy extends DiscountPolicy {
	private List<DiscountPolicy> discountPolicies = new ArrayList<>();
	public OverlappedDiscountPolicy(DiscountPolicy ... discountPolicies) {
		this.discountPolicies = Arrays.asList(discountPolicies);
	}

	@Override
	protected Money getDiscountAmount(Screeing screening) {
		Money result = Money.ZERO;
		for (DiscountPolicy each : discountPolicies) {
			result = result.plus(each.calculateDiscountAmount(screening));
		}
		return result;
	}
}
```

이 예제들은 Movie를 수정하지 않고도 새로운 기능을 추가하는 것이 얼마나 간단한지를 보여준다.

Movie가 DiscountPolicy라는 추상화에 의존하고, 생성자를 통해 DiscountPolicy에 대한 의존성을 명시적으로 드러냈으며, new와 같이 구체 클래스를 직접적으로 다뤄야 하는 책임을 Movie 외부로 옮겼기 때문이다.

### 조합 가능한 행동

어떤 객체와 협력하느냐에 따라 객체의 행동이 달라지는 것은 유연하고 재사용 가능한 설계가 가진 특징이다. 유연하고 재사용 가능한 설계는 응집도 높은 책임들을 가진 작은 객체들을 다양한 방식으로 연결함으로써 애플리케이션의 기능을 쉽게 확장할 수 있다.

훌륭한 객체지향 설계란 무엇을 하는지를 표현하는 것이 아니라 객체들의 조합을 선언적으로 표현함으로써 객체들이 무엇을 하는지를 표현하는 설계다.