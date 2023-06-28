import datetime
from corehq.apps.auditcare.models import AuditEvent as AE
from corehq.apps.auditcare.models import NavigationEventAudit as NE
from corehq.apps.auditcare.models import AccessAudit as AA
import zoneinfo
from django.db.models.functions import (
    TruncDate,
    TruncDay,
    TruncHour,
    TruncMinute,
    TruncSecond,
    TruncTime,
)
from django.test import override_settings
from django.db.models import Avg, Max, Min
import pytz
import zoneinfo

NE.objects.create(domain="jonathanlocal", user="jtang@dimagi.com", event_date=datetime.datetime.utcnow())

melb = zoneinfo.ZoneInfo("Australia/Melbourne")
domain = 'jonathantangtest'
jtangNE = NE.objects.filter(user__in=["jtang@dimagi.com", "emapson@dimagi.com"], event_date__gte=datetime.date.today())
staging_jonathan_testNE = NE.objects.filter(user="jonathan@test-61.commcarehq.org", event_date__gte=datetime.date.today())
with override_settings(USE_TZ=True):
    timezone_test = jtangNE.annotate(
        Adate=TruncDate('event_date', tzinfo=melb),
    ).values("user","Adate").annotate(min = Min('event_date'), max = Max('event_date'), rel_id = Min('id')).order_by("rel_id")
    print(list(timezone_test))
    for time in timezone_test:
        print(time)

with override_settings(USE_TZ=True):
    for d, dt in jtangNE.annotate(
        date=TruncHour('event_date', tzinfo=melb)
    ).values_list('date', 'event_date'):
        print(d.isoformat(), dt.isoformat())

for obj in jtangNE:
    print(obj.user, obj.domain, obj.view_fk.value, obj.event_date, obj.event_date.tzinfo)

for obj in staging_jonathan_testNE:
    print(obj.user, obj.domain, obj.view_fk.value, obj.params)

jtangAA = AA.objects.filter(user="jtang@dimagi.com", event_date__gte=datetime.date.today())
jonathan_testAA = AA.objects.filter(user="jonathan_test", event_date__gte=datetime.date.today())
staging_jonathanAA = AA.objects.filter(user="jonathan@test-61.commcarehq.org", event_date__gte=datetime.date.today())

for obj in jtangAA:
    print(obj.event_date, obj.access_type, obj.trace_id)

for obj in jonathan_testAA:
    print(obj.user, obj.domain, obj.event_date, obj.access_type, obj.trace_id)

staging_jonathanAA = AA.objects.filter(user="jonathan@test-61.commcarehq.org", event_date__gte=datetime.date.today())

from corehq.apps.domain.dbaccessors import iter_domains

from corehq.apps.domain.models import Domain

from corehq.apps.domain.dbaccessors import iter_domains
domain_obj = Domain.get_by_name("jonathanlocal")
domain_obj.default_timezone

domain_obj = Domain.get_by_name("jonathantangtest")

# def get_latest_apps(domain):

#     from corehq.apps.app_manager.models import Application
#     key = [domain]

#     results = Application.get_db().view(
#         'app_manager/applications_brief',
#         startkey=key + [{}],
#         endkey=key,
#         descending=True,
#         reduce=False,
#         include_docs=True,
#     ).all()

#     return results

# def web_apps_enabled(app):
#     try:
#         cloudcare_enabled = app['doc']['cloudcare_enabled']
#         return cloudcare_enabled
#     except KeyError:
#         return False

# def incomplete_forms_on(app):
#     try:
#         cc_show_incomplete = app['doc']['profile']['properties']['cc-show-incomplete']
#         return cc_show_incomplete == "yes"
#     except KeyError:
#         return False

# domains = iter_domains()

# for domain in domains:
#     apps = get_latest_apps(domain)
#     incomplete_forms_disabled_in_all_apps = True
#     web_apps_enabled_at_least_one_app = False

#     for app in apps:
#         if incomplete_forms_on(app):
#             incomplete_forms_disabled_in_all_apps = False
#         if web_apps_enabled(app):
#             web_apps_enabled_at_least_one_app = True
#     if incomplete_forms_disabled_in_all_apps and web_apps_enabled_at_least_one_app:
#         print(domain)
