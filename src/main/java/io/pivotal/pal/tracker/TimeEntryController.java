package io.pivotal.pal.tracker;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.metrics.CounterService;
import org.springframework.boot.actuate.metrics.GaugeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.websocket.server.PathParam;
import java.util.List;

@RestController
public class TimeEntryController {

    private final CounterService counter;
    private final GaugeService gauge;
    TimeEntryRepository timeEntryRepository;

    public TimeEntryController(@Autowired TimeEntryRepository timeEntryRepository, CounterService counter, GaugeService gauge) {
        this.timeEntryRepository = timeEntryRepository;
        this.counter = counter;
        this.gauge = gauge;
    }

    @PostMapping(value="/time-entries")
    public @ResponseBody ResponseEntity create(@RequestBody TimeEntry timeEntry) {

        TimeEntry newTimeEntry = timeEntryRepository.create(timeEntry);

        counter.increment("TimeEntry.created");
        gauge.submit("timeEntries.count", timeEntryRepository.list().size());

        return new ResponseEntity<TimeEntry>(newTimeEntry, HttpStatus.CREATED);
    }

    @GetMapping(value = "/time-entries/{id}")
    public @ResponseBody ResponseEntity<TimeEntry> read(@PathVariable("id") Long id) {

        TimeEntry timeEntry = timeEntryRepository.find(id);

        if (timeEntry != null) {
            counter.increment("TimeEntry.read");
            return new ResponseEntity<TimeEntry>(timeEntry, HttpStatus.OK);
        } else {
            return new ResponseEntity<TimeEntry>((TimeEntry)null, HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping(value = "/time-entries")
    public @ResponseBody ResponseEntity<List<TimeEntry>> list() {
        List<TimeEntry> results = timeEntryRepository.list();
        counter.increment("TimeEntry.listed");

        return new ResponseEntity<List<TimeEntry>>(results, HttpStatus.OK);
    }


    @PutMapping(value="/time-entries/{id}")
    public @ResponseBody ResponseEntity update(@PathVariable("id") Long id, @RequestBody TimeEntry timeEntry) {

        TimeEntry updatedTimeEntry = timeEntryRepository.update(id, timeEntry);

        if (updatedTimeEntry != null) {
            counter.increment("TimeEntry.updated");
            return new ResponseEntity<TimeEntry>(updatedTimeEntry, HttpStatus.OK);

        } else {
            return new ResponseEntity<TimeEntry>(timeEntry, HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping(value="/time-entries/{id}")
    public @ResponseBody ResponseEntity<TimeEntry> delete(@PathVariable("id") Long id) {

        TimeEntry deletedTimeEntry = timeEntryRepository.find(id);
        counter.increment("TimeEntry.deleted");
        gauge.submit("timeEntries.count", timeEntryRepository.list().size());
        timeEntryRepository.delete(id);

        return new ResponseEntity<TimeEntry>(deletedTimeEntry, HttpStatus.NO_CONTENT);

    }
}
