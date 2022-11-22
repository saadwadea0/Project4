package com.udacity.project4.locationreminders.savereminder

import android.app.Application
import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.R
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.locationreminders.DummyReminderData
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.getOrAwaitValue
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.hamcrest.core.Is
import org.junit.*
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@Config(sdk = [Build.VERSION_CODES.P])
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    // Set the main coroutines dispatcher for unit testing.
    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private lateinit var fakeDataSource: FakeDataSource

    private lateinit var viewModel: SaveReminderViewModel

    private lateinit var app: Application

    @Before
    fun setUp() {
        stopKoin()
        fakeDataSource = FakeDataSource()

        app = ApplicationProvider.getApplicationContext()

        viewModel = SaveReminderViewModel(app, fakeDataSource)
    }

    @Test
    fun saveReminder_ShowLoading() = runBlockingTest {
        // Pause dispatcher so you can verify initial values.
        mainCoroutineRule.pauseDispatcher()

        // WHEN save reminder
        viewModel.saveReminder(DummyReminderData.reminderDataItem)

        // THEN: the progress indicator is shown.
        MatcherAssert.assertThat(viewModel.showLoading.getOrAwaitValue(), CoreMatchers.`is`(true))

        // Execute pending coroutines actions.
        mainCoroutineRule.resumeDispatcher()

        // THEN: the progress indicator is hidden.
        MatcherAssert.assertThat(viewModel.showLoading.getOrAwaitValue(), CoreMatchers.`is`(false))
    }

    @Test
    fun saveReminder_Success() {
        // WHEN save reminder
        viewModel.saveReminder(DummyReminderData.reminderDataItem)

        MatcherAssert.assertThat(
            viewModel.showToast.getOrAwaitValue(),
            CoreMatchers.`is`(app.getString(R.string.reminder_saved))
        )
        Assert.assertEquals(viewModel.navigationCommand.getOrAwaitValue(), NavigationCommand.Back)
    }

    @Test
    fun validateEnteredData_TitleEmpty_ReturnFalse(){
        // GIVEN reminder with empty title
        val reminderData = DummyReminderData.reminderDataItem.copy()
        reminderData.title = ""

        // WHEN
        val res = viewModel.validateEnteredData(reminderData)

        // THEN
        MatcherAssert.assertThat(
            viewModel.showSnackBarInt.getOrAwaitValue(),
            CoreMatchers.`is`(R.string.err_enter_title)
        )
        MatcherAssert.assertThat(res, CoreMatchers.`is`(false))
    }

    @Test
    fun validateEnteredData_TitleNull_ReturnFalse(){
        // GIVEN reminder with empty title
        val reminderData = DummyReminderData.reminderDataItem.copy()
        reminderData.title = null

        // WHEN
        val res = viewModel.validateEnteredData(reminderData)

        // THEN
        MatcherAssert.assertThat(
            viewModel.showSnackBarInt.getOrAwaitValue(),
            CoreMatchers.`is`(R.string.err_enter_title)
        )
        MatcherAssert.assertThat(res, CoreMatchers.`is`(false))
    }

    @Test
    fun validateEnteredData_LocationNull_ReturnFalse(){
        // GIVEN reminder with empty title
        val reminderData = DummyReminderData.reminderDataItem.copy()
        reminderData.location = null

        // WHEN
        val res = viewModel.validateEnteredData(reminderData)

        // THEN
        MatcherAssert.assertThat(
            viewModel.showSnackBarInt.getOrAwaitValue(),
            Matchers.`is`(R.string.err_select_location)
        )
        MatcherAssert.assertThat(res, CoreMatchers.`is`(false))
    }

    @Test
    fun validateEnteredData_LocationEmpty_ReturnFalse(){
        // GIVEN reminder with empty title
        val reminderData = DummyReminderData.reminderDataItem.copy()
        reminderData.location = ""

        // WHEN
        val res = viewModel.validateEnteredData(reminderData)

        // THEN
        MatcherAssert.assertThat(
            viewModel.showSnackBarInt.getOrAwaitValue(),
            Matchers.`is`(R.string.err_select_location)
        )
        MatcherAssert.assertThat(res, CoreMatchers.`is`(false))
    }

    @Test
    fun validateEnteredData_ReturnTrue() {
        // GIVEN
        val res = viewModel.validateEnteredData(DummyReminderData.reminderDataItem)

        // THEN
        MatcherAssert.assertThat(res, CoreMatchers.`is`(true))
    }
}