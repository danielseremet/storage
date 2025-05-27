import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpResponse, provideHttpClient } from '@angular/common/http';
import { FormBuilder } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Subject, from, of } from 'rxjs';

import { IUser } from 'app/entities/user/user.model';
import { UserService } from 'app/entities/user/service/user.service';
import { StorageFileService } from '../service/storage-file.service';
import { IStorageFile } from '../storage-file.model';
import { StorageFileFormService } from './storage-file-form.service';

import { StorageFileUpdateComponent } from './storage-file-update.component';

describe('StorageFile Management Update Component', () => {
  let comp: StorageFileUpdateComponent;
  let fixture: ComponentFixture<StorageFileUpdateComponent>;
  let activatedRoute: ActivatedRoute;
  let storageFileFormService: StorageFileFormService;
  let storageFileService: StorageFileService;
  let userService: UserService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [StorageFileUpdateComponent],
      providers: [
        provideHttpClient(),
        FormBuilder,
        {
          provide: ActivatedRoute,
          useValue: {
            params: from([{}]),
          },
        },
      ],
    })
      .overrideTemplate(StorageFileUpdateComponent, '')
      .compileComponents();

    fixture = TestBed.createComponent(StorageFileUpdateComponent);
    activatedRoute = TestBed.inject(ActivatedRoute);
    storageFileFormService = TestBed.inject(StorageFileFormService);
    storageFileService = TestBed.inject(StorageFileService);
    userService = TestBed.inject(UserService);

    comp = fixture.componentInstance;
  });

  describe('ngOnInit', () => {
    it('should call User query and add missing value', () => {
      const storageFile: IStorageFile = { id: 11476 };
      const user: IUser = { id: 3944 };
      storageFile.user = user;

      const userCollection: IUser[] = [{ id: 3944 }];
      jest.spyOn(userService, 'query').mockReturnValue(of(new HttpResponse({ body: userCollection })));
      const additionalUsers = [user];
      const expectedCollection: IUser[] = [...additionalUsers, ...userCollection];
      jest.spyOn(userService, 'addUserToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ storageFile });
      comp.ngOnInit();

      expect(userService.query).toHaveBeenCalled();
      expect(userService.addUserToCollectionIfMissing).toHaveBeenCalledWith(
        userCollection,
        ...additionalUsers.map(expect.objectContaining),
      );
      expect(comp.usersSharedCollection).toEqual(expectedCollection);
    });

    it('should update editForm', () => {
      const storageFile: IStorageFile = { id: 11476 };
      const user: IUser = { id: 3944 };
      storageFile.user = user;

      activatedRoute.data = of({ storageFile });
      comp.ngOnInit();

      expect(comp.usersSharedCollection).toContainEqual(user);
      expect(comp.storageFile).toEqual(storageFile);
    });
  });

  describe('save', () => {
    it('should call update service on save for existing entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IStorageFile>>();
      const storageFile = { id: 11027 };
      jest.spyOn(storageFileFormService, 'getStorageFile').mockReturnValue(storageFile);
      jest.spyOn(storageFileService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ storageFile });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: storageFile }));
      saveSubject.complete();

      // THEN
      expect(storageFileFormService.getStorageFile).toHaveBeenCalled();
      expect(comp.previousState).toHaveBeenCalled();
      expect(storageFileService.update).toHaveBeenCalledWith(expect.objectContaining(storageFile));
      expect(comp.isSaving).toEqual(false);
    });

    it('should call create service on save for new entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IStorageFile>>();
      const storageFile = { id: 11027 };
      jest.spyOn(storageFileFormService, 'getStorageFile').mockReturnValue({ id: null });
      jest.spyOn(storageFileService, 'create').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ storageFile: null });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: storageFile }));
      saveSubject.complete();

      // THEN
      expect(storageFileFormService.getStorageFile).toHaveBeenCalled();
      expect(storageFileService.create).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).toHaveBeenCalled();
    });

    it('should set isSaving to false on error', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IStorageFile>>();
      const storageFile = { id: 11027 };
      jest.spyOn(storageFileService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ storageFile });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.error('This is an error!');

      // THEN
      expect(storageFileService.update).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).not.toHaveBeenCalled();
    });
  });

  describe('Compare relationships', () => {
    describe('compareUser', () => {
      it('should forward to userService', () => {
        const entity = { id: 3944 };
        const entity2 = { id: 6275 };
        jest.spyOn(userService, 'compareUser');
        comp.compareUser(entity, entity2);
        expect(userService.compareUser).toHaveBeenCalledWith(entity, entity2);
      });
    });
  });
});
