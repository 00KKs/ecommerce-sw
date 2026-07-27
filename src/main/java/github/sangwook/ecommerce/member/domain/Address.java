package github.sangwook.ecommerce.member.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

@Entity
@Table(name = "address")
@Getter
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "recipient_name")
    private String recipientName;

    @Column(name = "recipient_phone")
    private String recipientPhone;

    @Column(name = "address")
    private String address;

    @Column(name = "delivery_request")
    private String deliveryRequest;

    @Column(name = "is_default")
    private Boolean isDefault;

    protected Address() {
    }

    public Address(Long memberId, String deliveryRequest, String recipientPhone, String recipientName, String address, boolean isDefault) {
        this.memberId = memberId;
        this.deliveryRequest = deliveryRequest;
        this.recipientPhone = recipientPhone;
        this.recipientName = recipientName;
        this.address = address;
        this.isDefault = isDefault;
    }

    public void setAsDefault() {
        this.isDefault = true;
    }

    public void unSetDefault() {
        this.isDefault = false;
    }

    public void update(String recipientName, String recipientPhone, String address, String deliveryRequest) {
        this.recipientName = recipientName;
        this.recipientPhone = recipientPhone;
        this.address = address;
        this.deliveryRequest = deliveryRequest;
    }
}
