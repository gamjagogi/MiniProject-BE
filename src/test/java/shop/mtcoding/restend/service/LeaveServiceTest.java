package shop.mtcoding.restend.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import shop.mtcoding.restend.core.dummy.DummyEntity;
import shop.mtcoding.restend.dto.leave.LeaveRequest;
import shop.mtcoding.restend.dto.leave.LeaveResponse;
import shop.mtcoding.restend.model.alarm.Alarm;
import shop.mtcoding.restend.model.alarm.AlarmRepository;
import shop.mtcoding.restend.model.leave.Leave;
import shop.mtcoding.restend.model.leave.LeaveRepository;
import shop.mtcoding.restend.model.leave.enums.LeaveType;
import shop.mtcoding.restend.model.user.User;
import shop.mtcoding.restend.model.user.UserRepository;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static shop.mtcoding.restend.model.leave.enums.LeaveStatus.*;
import static shop.mtcoding.restend.model.leave.enums.LeaveType.ANNUAL;
import static shop.mtcoding.restend.model.leave.enums.LeaveType.DUTY;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class LeaveServiceTest extends DummyEntity {
    @InjectMocks
    private LeaveService leaveService;

    // 가짜 객체를 만들어서 Mockito 환경에 Load
    @Mock
    private UserRepository userRepository;
    @Mock
    private LeaveRepository leaveRepository;
    @Mock
    private AlarmRepository alarmRepository;

    @Test
    public void 연차당직신청하기_test() throws Exception{

        // given
        LeaveRequest.ApplyInDTO applyInDTO = new LeaveRequest.ApplyInDTO();
        applyInDTO.setType(LeaveType.valueOf("DUTY"));
        applyInDTO.setStartDate(LocalDate.parse("2023-07-20"));
        applyInDTO.setEndDate(LocalDate.parse("2023-07-20"));

        // stub 1
        User cos = newMockUser(1L, "cos", 15);
        Mockito.when(userRepository.findById(any())).thenReturn(Optional.of(cos));

        // stub 2
        Alarm alarm = newMockAlarm(1L, cos, "당직이 등록되었습니다.");
        Mockito.when(alarmRepository.save(any())).thenReturn(alarm);

        // stub 3
        Leave leave = newMockLeave(1L, cos, LeaveType.DUTY,  LocalDate.parse("2023-07-20"), LocalDate.parse("2023-07-20"), 0);
        Mockito.when(leaveRepository.save(any())).thenReturn(leave);

        // when
        LeaveResponse.ApplyOutDTO applyOutDTO = leaveService.연차당직신청하기(applyInDTO, 1L);

        // then
        Assertions.assertThat(applyOutDTO.getId()).isEqualTo(1L);
        Assertions.assertThat(applyOutDTO.getType()).isEqualTo(LeaveType.DUTY);
        Assertions.assertThat(applyOutDTO.getUsingDays()).isEqualTo(0);
        Assertions.assertThat(applyOutDTO.getRemainDays()).isEqualTo(15);
        Assertions.assertThat(applyOutDTO.getStatus()).isEqualTo(WAITING);
    }

    @Test
    public void 연차당직신청취소하기_test() throws Exception{
        // given
        Long id = 1L;

        // stub 1
        User cos = newMockUser(1L, "cos", 8);
        Mockito.when(userRepository.findById(any())).thenReturn(Optional.of(cos));

        // stub 2
        Alarm alarm = newMockAlarm(1L, cos, "cos님의 2023-07-20부터 2023-07-20까지, 총 1일의 연차 신청이 취소되었습니다.");
        Mockito.when(alarmRepository.save(any())).thenReturn(alarm);

        // stub 3
        Leave leave = newMockLeave(1L, cos, ANNUAL,  LocalDate.parse("2023-07-20"), LocalDate.parse("2023-07-20"), 1);
        Mockito.when(leaveRepository.findById(any())).thenReturn(Optional.of(leave));

        // when
        LeaveResponse.CancelOutDTO cancelOutDTO = leaveService.연차당직신청취소하기(1L, 1L);

        // then
        Assertions.assertThat(cancelOutDTO.getRemainDays()).isEqualTo(9);
    }

    @Test
    public void 모든유저특정월기준정보_Test() {

        User user1 = User.builder().id(1L).username("dotori").build();
        User user2 = User.builder().id(2L).username("tomato").build();
        User user3 = User.builder().id(3L).username("strawberry").build();

        Leave leave1 = Leave.builder()
                .user(user1)
                .type(ANNUAL)
                .startDate(LocalDate.of(2023, 4, 25))
                .endDate(LocalDate.of(2023, 5, 5))
                .status(WAITING)
                .build();

        Leave leave2 = Leave.builder()
                .user(user2)
                .type(DUTY)
                .startDate(LocalDate.of(2023, 5, 3))
                .endDate(LocalDate.of(2023, 5, 4))
                .status(APPROVAL)
                .build();

        Leave leave3 = Leave.builder()
                .user(user3)
                .type(ANNUAL)
                .startDate(LocalDate.of(2023, 5, 27))
                .endDate(LocalDate.of(2023, 6, 5))
                .status(REJECTION)
                .build();

        List<Leave> leaveList = Arrays.asList(leave1, leave2, leave3);

        //Mockito.when(leaveRepository.findAllByUserId(any())).thenReturn(leaveList);
        Mockito.when(leaveRepository.findAll()).thenReturn(leaveList);

        // when
        List<LeaveResponse.InfoOutDTO> result = leaveService.getLeaves(null, "2023-05-15", null, null);

        // then
        assertEquals(3, result.size());
        assertEquals(leave1.getUser().getId(), result.get(0).getUserId());
        assertEquals(leave1.getUser().getUsername(), result.get(0).getUsername());
        assertEquals(leave1.getType(), result.get(0).getType());
        assertEquals(leave1.getStatus(), result.get(0).getStatus());
        assertEquals(leave1.getStartDate().toString(), result.get(0).getStartDate().toString());
        assertEquals(leave1.getEndDate().toString(), result.get(0).getEndDate().toString());
        assertEquals(leave2.getUser().getId(), result.get(1).getUserId());
        assertEquals(leave2.getUser().getUsername(), result.get(1).getUsername());
        assertEquals(leave2.getType(), result.get(1).getType());
        assertEquals(leave2.getStatus(), result.get(1).getStatus());
        assertEquals(leave2.getStartDate().toString(), result.get(1).getStartDate().toString());
        assertEquals(leave2.getEndDate().toString(), result.get(1).getEndDate().toString());
    }

    @Test
    public void 특정유저특정월정보_Test() {
        // given
        User user1 = User.builder().id(1L).username("dotori").build();
        //User user2 = User.builder().id(2L).build();

        Leave leave1 = Leave.builder()
                .user(user1)
                .type(ANNUAL)
                .startDate(LocalDate.of(2023, 4, 25))
                .endDate(LocalDate.of(2023, 5, 5))
                .status(WAITING)
                .build();

        Leave leave2 = Leave.builder()
                .user(user1)
                .type(DUTY)
                .startDate(LocalDate.of(2023, 5, 3))
                .endDate(LocalDate.of(2023, 5, 4))
                .status(APPROVAL)
                .build();

        Leave leave3 = Leave.builder()
                .user(user1)
                .type(ANNUAL)
                .startDate(LocalDate.of(2023, 5, 27))
                .endDate(LocalDate.of(2023, 6, 5))
                .status(REJECTION)
                .build();

        List<Leave> leaveList = Arrays.asList(leave1, leave2, leave3);

        Mockito.when(leaveRepository.findAllByUserId(any())).thenReturn(leaveList);
        //Mockito.when(leaveRepository.findAll()).thenReturn(leaveList);

        // when
        List<LeaveResponse.InfoOutDTO> result = leaveService.getLeaves(1L, "2023-05-15", null, null);

        // then
        assertEquals(3, result.size());
        assertEquals(leave1.getUser().getId(), result.get(0).getUserId());
        assertEquals(leave1.getUser().getUsername(), result.get(0).getUsername());
        assertEquals(leave1.getType(), result.get(0).getType());
        assertEquals(leave1.getStatus(), result.get(0).getStatus());
        assertEquals(leave1.getStartDate().toString(), result.get(0).getStartDate().toString());
        assertEquals(leave1.getEndDate().toString(), result.get(0).getEndDate().toString());
        assertEquals(leave2.getUser().getId(), result.get(1).getUserId());
        assertEquals(leave2.getUser().getUsername(), result.get(1).getUsername());
        assertEquals(leave2.getType(), result.get(1).getType());
        assertEquals(leave2.getStatus(), result.get(1).getStatus());
        assertEquals(leave2.getStartDate().toString(), result.get(1).getStartDate().toString());
        assertEquals(leave2.getEndDate().toString(), result.get(1).getEndDate().toString());
    }

    @Test
    public void 특정유저주단위정보_Test() {
        // given
        User user1 = User.builder().id(1L).username("dotori").build();

        Leave leave1 = Leave.builder()
                .user(user1)
                .type(ANNUAL)
                .startDate(LocalDate.of(2023, 4, 25))
                .endDate(LocalDate.of(2023, 5, 5))
                .status(WAITING)
                .build();

        Leave leave2 = Leave.builder()
                .user(user1)
                .type(DUTY)
                .startDate(LocalDate.of(2023, 5, 10))
                .endDate(LocalDate.of(2023, 5, 14))
                .status(APPROVAL)
                .build();

        Leave leave3 = Leave.builder()
                .user(user1)
                .type(ANNUAL)
                .startDate(LocalDate.of(2023, 5, 27))
                .endDate(LocalDate.of(2023, 6, 5))
                .status(REJECTION)
                .build();

        List<Leave> leaveList = Arrays.asList(leave1, leave2, leave3);

        Mockito.when(leaveRepository.findAllByUserId(any())).thenReturn(leaveList);

        // when
        List<LeaveResponse.InfoOutDTO> result = leaveService.getLeaves(1L, null, "2023-05-15", null);

        // then
        assertEquals(1, result.size());
        assertEquals(leave2.getUser().getId(), result.get(0).getUserId());
        assertEquals(leave2.getUser().getUsername(), result.get(0).getUsername());
        assertEquals(leave2.getType(), result.get(0).getType());
        assertEquals(leave2.getStatus(), result.get(0).getStatus());
        assertEquals(leave2.getStartDate().toString(), result.get(0).getStartDate().toString());
        assertEquals(leave2.getEndDate().toString(), result.get(0).getEndDate().toString());
    }

    @Test
    public void 특정유저특정일단위_Test() {
        // given
        User user1 = User.builder().id(1L).username("dotori").build();

        Leave leave1 = Leave.builder()
                .user(user1)
                .type(ANNUAL)
                .startDate(LocalDate.of(2023, 4, 25))
                .endDate(LocalDate.of(2023, 5, 5))
                .status(WAITING)
                .build();

        Leave leave2 = Leave.builder()
                .user(user1)
                .type(DUTY)
                .startDate(LocalDate.of(2023, 5, 10))
                .endDate(LocalDate.of(2023, 5, 14))
                .status(APPROVAL)
                .build();

        Leave leave3 = Leave.builder()
                .user(user1)
                .type(ANNUAL)
                .startDate(LocalDate.of(2023, 5, 27))
                .endDate(LocalDate.of(2023, 6, 5))
                .status(REJECTION)
                .build();

        List<Leave> leaveList = Arrays.asList(leave1, leave2, leave3);

        Mockito.when(leaveRepository.findAllByUserId(any())).thenReturn(leaveList);

        // when
        List<LeaveResponse.InfoOutDTO> result = leaveService.getLeaves(1L, null, null, "2023-06-01");

        // then
        assertEquals(1, result.size());
        assertEquals(leave3.getUser().getId(), result.get(0).getUserId());
        assertEquals(leave3.getUser().getUsername(), result.get(0).getUsername());
        assertEquals(leave3.getType(), result.get(0).getType());
        assertEquals(leave3.getStatus(), result.get(0).getStatus());
        assertEquals(leave3.getStartDate().toString(), result.get(0).getStartDate().toString());
        assertEquals(leave3.getEndDate().toString(), result.get(0).getEndDate().toString());
    }
}
