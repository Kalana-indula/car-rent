package com.demo.carrent.service;

import com.demo.carrent.common.PaymentStatus;
import com.demo.carrent.common.RentStatus;
import com.demo.carrent.dto.PaymentDto;
import com.demo.carrent.dto.response.CreateResponse;
import com.demo.carrent.dto.response.UpdateResponse;
import com.demo.carrent.entity.Payment;
import com.demo.carrent.entity.Rent;
import com.demo.carrent.repository.PaymentRepository;
import com.demo.carrent.repository.RentRepository;
import com.demo.carrent.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;


@Service
public class PaymentServiceImpl implements PaymentService{

    private final PaymentRepository paymentRepository;

    private final UserRepository userRepository;

    private final RentRepository rentRepository;

    @Autowired
    public PaymentServiceImpl(PaymentRepository paymentRepository,UserRepository userRepository,RentRepository rentRepository){
        this.paymentRepository=paymentRepository;
        this.userRepository=userRepository;
        this.rentRepository=rentRepository;
    }

    @Override
    public CreateResponse<Payment> createPayment(PaymentDto paymentDto) {

        //find user and rent
        Rent rent=rentRepository.findById(paymentDto.getRentId()).orElse(null);

        CreateResponse<Payment> paymentCreateResponse=new CreateResponse<>();

        if(rent!=null){
            //check if the rent is approved
            if(rent.getRentStatus()==RentStatus.APPROVED){
                //create new payment
                Payment payment=new Payment();

                payment.setAmount(rent.getPrice());
                payment.setRent(rent);
                payment.setUser(rent.getUser());

                paymentRepository.save(payment);

                //set payment status in rent to 'PAID'
                rent.setPaymentStatus(PaymentStatus.PAID);
                rentRepository.save(rent);

                paymentCreateResponse.setStatusMessage("Payment create successfully");
                paymentCreateResponse.setCreatedData(payment);

                return paymentCreateResponse;
            }else{
                paymentCreateResponse.setStatusMessage("Rent is not approved");
                return paymentCreateResponse;
            }

        }else{
            paymentCreateResponse.setStatusMessage("No valid rent found");

            return paymentCreateResponse;
        }

    }

    @Override
    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    @Override
    public UpdateResponse<Payment> updatePayment(Long rentId) {

        UpdateResponse<Payment> paymentUpdateResponse = new UpdateResponse<>();

        //find current rent
        Rent existingRent = rentRepository.findById(rentId).orElse(null);

        if (existingRent != null) {
            //check if rent status is 'APPROVED'
            if (existingRent.getRentStatus() == RentStatus.APPROVED) {

                //find current rent amount
                BigDecimal newAmount = existingRent.getPrice();

                //find current paid amount
                Payment currentPayment = existingRent.getPayment();

                if (currentPayment != null) {
                    //find paid rent amount
                    BigDecimal paidAmount = currentPayment.getAmount();

                    //find outstanding
                    BigDecimal outstandingAmount = newAmount.subtract(paidAmount);

                    currentPayment.setAmount(outstandingAmount);
                    currentPayment.setRent(existingRent);
                    currentPayment.setUser(existingRent.getUser());

                    paymentRepository.save(currentPayment);

                    paymentUpdateResponse.setUpdatedData(currentPayment);
                } else {

                    Payment newPayment = new Payment();

                    newPayment.setAmount(newAmount);
                    newPayment.setRent(existingRent);
                    newPayment.setUser(existingRent.getUser());

                    paymentRepository.save(newPayment);

                    paymentUpdateResponse.setUpdatedData(newPayment);
                }

                //update rent
                existingRent.setPaymentStatus(PaymentStatus.PAID);
                rentRepository.save(existingRent);

                paymentUpdateResponse.setResponseMessage("Payment updated successfully");

                return paymentUpdateResponse;

            } else {
                paymentUpdateResponse.setResponseMessage("Rent was not approved");

                return paymentUpdateResponse;
            }
        } else {
            paymentUpdateResponse.setResponseMessage("Invalid rent id");

            return paymentUpdateResponse;
        }
    }
}
