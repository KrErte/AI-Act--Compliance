import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { ApiService } from './api.service';
import { environment } from '@env';

describe('ApiService', () => {
  let service: ApiService;
  let httpMock: HttpTestingController;

  const BASE_URL = environment.apiUrl;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [ApiService]
    });

    service = TestBed.inject(ApiService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  describe('get', () => {
    it('should make a GET request to the correct URL', () => {
      service.get('/test-endpoint').subscribe(res => {
        expect(res.success).toBeTrue();
      });

      const req = httpMock.expectOne(`${BASE_URL}/test-endpoint`);
      expect(req.request.method).toBe('GET');
      req.flush({ success: true, data: {}, timestamp: new Date().toISOString() });
    });

    it('should append query params when provided', () => {
      service.get('/search', { q: 'hello', page: 1, active: true }).subscribe();

      const req = httpMock.expectOne(r =>
        r.url === `${BASE_URL}/search` &&
        r.params.get('q') === 'hello' &&
        r.params.get('page') === '1' &&
        r.params.get('active') === 'true'
      );
      expect(req.request.method).toBe('GET');
      req.flush({ success: true, data: {}, timestamp: new Date().toISOString() });
    });

    it('should make a GET request without params when none are provided', () => {
      service.get('/items').subscribe();

      const req = httpMock.expectOne(`${BASE_URL}/items`);
      expect(req.request.params.keys().length).toBe(0);
      req.flush({ success: true, data: [], timestamp: new Date().toISOString() });
    });
  });

  describe('getPaged', () => {
    it('should make a paged GET request with params', () => {
      service.getPaged('/items', { page: 0, size: 10 }).subscribe(res => {
        expect(res.success).toBeTrue();
      });

      const req = httpMock.expectOne(r =>
        r.url === `${BASE_URL}/items` &&
        r.params.get('page') === '0' &&
        r.params.get('size') === '10'
      );
      expect(req.request.method).toBe('GET');
      req.flush({
        success: true,
        data: { content: [], page: 0, size: 10, totalElements: 0, totalPages: 0, last: true },
        timestamp: new Date().toISOString()
      });
    });
  });

  describe('post', () => {
    it('should make a POST request with the given body', () => {
      const body = { name: 'Test', value: 42 };

      service.post('/create', body).subscribe(res => {
        expect(res.success).toBeTrue();
      });

      const req = httpMock.expectOne(`${BASE_URL}/create`);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(body);
      req.flush({ success: true, data: { id: '1' }, timestamp: new Date().toISOString() });
    });

    it('should send empty object as body when no body is provided', () => {
      service.post('/trigger').subscribe();

      const req = httpMock.expectOne(`${BASE_URL}/trigger`);
      expect(req.request.body).toEqual({});
      req.flush({ success: true, timestamp: new Date().toISOString() });
    });
  });

  describe('put', () => {
    it('should make a PUT request with the given body', () => {
      const body = { name: 'Updated' };

      service.put('/items/1', body).subscribe();

      const req = httpMock.expectOne(`${BASE_URL}/items/1`);
      expect(req.request.method).toBe('PUT');
      expect(req.request.body).toEqual(body);
      req.flush({ success: true, timestamp: new Date().toISOString() });
    });
  });

  describe('patch', () => {
    it('should make a PATCH request with the given body', () => {
      const body = { status: 'ACTIVE' };

      service.patch('/items/1', body).subscribe();

      const req = httpMock.expectOne(`${BASE_URL}/items/1`);
      expect(req.request.method).toBe('PATCH');
      expect(req.request.body).toEqual(body);
      req.flush({ success: true, timestamp: new Date().toISOString() });
    });
  });

  describe('delete', () => {
    it('should make a DELETE request to the correct URL', () => {
      service.delete('/items/1').subscribe();

      const req = httpMock.expectOne(`${BASE_URL}/items/1`);
      expect(req.request.method).toBe('DELETE');
      req.flush({ success: true, timestamp: new Date().toISOString() });
    });
  });
});
