# 9장. 유연한 설계

## 1. 개방-폐쇄 원칙

로버트 마틴은 확장 가능하고 변화에 유연하게 대응할 수 있는 설계를 만들 수 있는 개방-폐쇄 원칙(Open-Closed Principle, OCP)을 고안했다.

소프트웨어 개체(클래스, 모듈, 함수 등등)는 확장에 대해 열려 있어야 하고, 수정에 대해서는 닫혀 있어야 한다.

- 확장에 대해 열려 있다.
    - 애플리케이션의 요구사항이 변경될 때 이 변경에 맞게 새로운 ‘동작’을 추가해서 기능을 확장할 수 있다.
- 수정에 대해 닫혀 있다.
    - 기존의 ‘코드’를 수정하지 않고도 동작을 추가하거나 변경할 수 있다.

### 컴파일타임 의존성을 고정시키고 런타임 의존성을 변경하라

영화 예매 시스템은 이미 개방-폐쇄 원칙을 따른다. 컴파일타임 의존성 관점에서는 Movie 클래스는 추상 클래스인 DiscountPolicy에 의존한다. 런타임 의존성 관점에서 Movie 인스턴스는 AmountDiscountPolicy와 PercentDiscountPolicy 인스턴스에 의존한다.

중복 할인 정책을 추가하기 위해 한 일은 DiscountPolicy의 자식 클래스로 OverlappedDiscountPolicy 클래스를 추가한 것 뿐이다. 기존 코드 중 어떤 것도 수정하지 않았다.

[그림9-2]

### 추상화가 핵심이다.

개방-폐쇄 원칙의 핵심은 추상화에 의존하는 것이다.

Movie는 할인 정책을 추상화한 DIscountPolicy만 의존한다. 의존성은 변경의 영향을 의미하고 DiscountPolicy는 변하지 않는 추상화다. Movie는 안정된 추상화인 DiscountPolicy에 의존하기 떄문에 할인 정책을 추가하기 위해 DiscountPolicy의 자식 클래스를 추가하더라도 영향을 받지 않는다. 따라서 Movie와 DiscountPolicy는 수정에 닫혀 있다.

여기서 주의할 점은 추상화를 했다고 해서 모든 수정에 대해 설계가 폐쇄되는 것은 아니다. 변경에 의한 파급효과를 최대한 피하기 위해서는 변하는 것과 반하지 않는 것이 무엇인지를 이해하고 이를 추상화의 목적으로 삼아야만 한다.

## 2. 생성 사용 분리

Movie가 오직 DiscountPolicy라는 추상화에만 의존하기 위해서는 Movie 내부에서 AmountDiscountPolicy같은 구체 클래스의 인스턴스를 생성해서는 안 된다. 할인 정책을 바꾸기 위해서는 AmountDiscountPolicy의 인스턴스 생성하는 부분을 변경하는 방법밖에 없으므로 직접 코드를 수정하는 것뿐이다. 이것은 개방-폐쇄 원칙을 위반한다.

물론 객체 생성을 피할 수는 없다. 어디선가 반드시 객체를 생성해야 한다. 문제는 객체 생성이 아니다. 부적절한 곳에서 객체를 생성하는 것이 문제다. 동일한 클래스 안에서 객체 생성과 사용이라는 두 가지 이질적인 목적을 가진 코드가 공존하는 것이 문제이다.

객체에 대한 생성과 사용(separating use from creation)을 분리해야 한다.

사용으로부터 생성을 분리하는 데 가장 보편적인 방법은 객체를 생성할 챔임을 클라이언트로 옮기는 것이다.

```java
public class Client {
	public Money getAvatarFee() {
		Movie avatar = new Movie(...);
		return avatar.getFee();
	}
}
```

[그림9-5]

### FACTORY 추가하기

생성 책임을 클라이언트로 옮긴 배경에는 Movie는 특정 컨텍스트에 묶여서는 안 되지만 클라이언트는 묶여도 상관 없다는 전체가 깔려 있다. 

위 Client 코드를 보면 Movie의 인스턴스 생성과 동시에 getFee 메시지도 함께 전송한다는 것을 알 수 있다. Client 역시 생성과 사용의 책임을 함께 가지고 있다.

생성과 사용을 분리하기 위해 객체 생성에 특화된 객체를 FACTORY라고 한다.

```java
public class Factory{
	public Movie createAvatarMovie() {		
		return new Movie("아바타", ..., new AmountDiscountPolicy(...));
	}
}

public class Client {
	private Factory factory;
	public Client(Factory factory) {
		this.factory = factory;
	}

	public Money getAvatarFee() {
		Movie avatar = factory.createAvatarMovie()
		return avatar.getFee();
	}
}
```

FACTORY를 사용하면 Movie와 AmountDiscountPolicy를 생성하는 책임 모두를 FACTORY로 이동할 수 있다. Client는 오직 사용과 관련된 책임만 진다.

[그림9-6]

### 순수한 가공물에 책임 할당하기

책임 할당의 가장 기본이 되는 원칙은 책임을 수행하는 데 필요한 정보를 가장 많이 알고 있는 정보 전문가(Infomation Expert)에게 책임을 할당하는 것이다. 도메인 모델은 정보 전문가를 찾기 위해 참조할 수 있는 일차적인 재료다.

방금 전 FACTORY는 도메인 모델에 속하지 않는다. FACTORY를 추가한 이유는 순수하게 기술적인 결정이다. 전체적으로 결합도를 낮추고 재사용성을 높이기 위해 도메인 개념에게 할당돼 있던 객체 생성 책임을 도메인 개념과는 상관이 없는 가공의 객체로 이동시킨 것이다.

도메인 개념을 표현하는 객체에게 책임을 할당하는 것만으로는 부족한 경우가 발생한다. 실제로 동작하는 애플리케이션은 데이터베이스 접근을 위한 객체와 같이 도메인 개념들을 초월하는 기계적인 개념들을 필요로 할 수 있다.

책임을 할당하기 위해 창조되는 도메인과 무관한 인공적인 객체를 순수한 가공물(Pure Fabrication)이라고 부른다.

어떤 행동을 추가하려고 하는데 이 행동을 책임질 마땅한 도메인 개념이 존재하지 않는다면 Pure Fabrication을 추가하고 책임을 할당하라.

**Pure Fabrication 패턴**

정보 전문가 패턴에 따라 책임을 할당한 결과가 바람직하지 않을 경우 대안으로 사용된다. 어떤 객체가 책임을 수행하는 데 필요한 정보를 가졌지만 해당 책임을 할당할 경우 응집도가 낮아지고 결합도가 높아진다면 가공의 객체를 추가해서 책임을 옮기는 것을 고민하라.